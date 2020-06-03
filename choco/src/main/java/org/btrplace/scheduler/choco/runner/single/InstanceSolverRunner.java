/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.runner.single;

import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.Killed;
import org.btrplace.model.constraint.OptConstraint;
import org.btrplace.model.constraint.Ready;
import org.btrplace.model.constraint.Running;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.Sleeping;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.SchedulerModelingException;
import org.btrplace.scheduler.choco.DefaultReconfigurationProblemBuilder;
import org.btrplace.scheduler.choco.LifeCycleViolationException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.constraint.CObjective;
import org.btrplace.scheduler.choco.constraint.ChocoConstraint;
import org.btrplace.scheduler.choco.constraint.ChocoMapper;
import org.btrplace.scheduler.choco.runner.Metrics;
import org.btrplace.scheduler.choco.runner.SolutionStatistics;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;
import org.btrplace.scheduler.choco.view.ChocoView;
import org.btrplace.scheduler.choco.view.ChocoViews;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.SearchState;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.measure.Measures;
import org.chocosolver.solver.search.measure.MeasuresRecorder;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * A basic solver that solve a whole instance.
 * Use {@link #call()} to compute a solution
 *
 * @author Fabien Hermenier
 */
public class InstanceSolverRunner implements Callable<SolvingStatistics> {

    private final Parameters params;

    private ReconfigurationProblem rp;

    private final Collection<SatConstraint> cstrs;

    private final OptConstraint obj;

    private final Model origin;

    private final Instance instance;

    private SingleRunnerStatistics stats;

    /**
     * Choco version of the constraints.
     */
    private List<ChocoConstraint> cConstraints;

    private List<ChocoView> views;

    /**
     * Make a new runner.
     *
     * @param ps the parameters for the solving process
     * @param i  the instance to solve
     */
    public InstanceSolverRunner(Parameters ps, Instance i) {
        instance = i;
        cstrs = i.getSatConstraints();
        obj = i.getOptConstraint();
        origin = i.getModel();
        params = ps;
    }

    @Override
    @SuppressWarnings("squid:S1166") //for the LifeCycleViolationException
    public SolvingStatistics call() throws SchedulerException {
        stats = new SingleRunnerStatistics(params, instance, System.currentTimeMillis());
        rp = null;

        //Build the core problem
        long d = -System.currentTimeMillis();
        try {
            rp = buildRP();
        } catch (@SuppressWarnings("unused") LifeCycleViolationException ex) {
            //If there is a violation of the cycle it is not a bug that should be propagated
            //it it just indicating there is no solution
          stats.setCompleted(true);
            stats.setMetrics(new Metrics());
            return stats;
        } finally {
            d += System.currentTimeMillis();
            stats.setCoreBuildDuration(d);
        }
        stats.setNbManagedVMs(rp.getManageableVMs().size());

        //Customize the core problem
        d = -System.currentTimeMillis();
        if (!specialise()) {
          d += System.currentTimeMillis();
            stats.setSpecialisationDuration(d);
          stats.setCompleted(true);
            return getStatistics();
        }
        d += System.currentTimeMillis();
        stats.setSpecialisationDuration(d);

        //statistics
        stats.setMetrics(new Metrics(rp.getSolver().getMeasures()));
        rp.getLogger().debug(stats.toString());

        //The solution monitor to store the measures at each solution
        rp.getSolver().plugMonitor((IMonitorSolution) () -> {
            Solution solution = new Solution(rp.getModel());
            solution.record();

            ReconfigurationPlan plan = rp.buildReconfigurationPlan(solution, origin);
            views.forEach(v -> v.insertActions(rp, solution, plan));

            MeasuresRecorder m = rp.getSolver().getMeasures();
            SolutionStatistics st = new SolutionStatistics(new Metrics(m), plan);
            IntVar o = rp.getObjective();
            if (o != null) {
                st.setObjective(solution.getIntVal(o));
            }
            stats.addSolution(st);

          params.solutionListeners().forEach(c -> c.accept(rp, plan));
        });

        setVerbosity();

        //The actual solving process
        rp.solve(params.getTimeLimit(), params.doOptimize());
        return getStatistics();
    }


    private void setVerbosity() {
        if (params.getVerbosity() >= 2) {
            //every second
            rp.getSolver().showStatisticsDuringResolution(1000);
        }
        if (params.getVerbosity() >= 3) {
            rp.getSolver().showDecisions();
        }
        if (params.getVerbosity() >= 4) {
            rp.getSolver().showContradiction();
        }
    }

    private boolean specialise() {
        
        //Resolve the view dependencies, add them and inject them
        views = ChocoViews.resolveDependencies(origin, views, rp.getViews());
        views.forEach(rp::addView);
        //Inject the sat constraints, 2nd pass on the view. Then the objective for a late optimisation
        Optional<ChocoConstraint> o = cConstraints.stream().filter(c -> c instanceof CObjective).findFirst();
        return views.stream().allMatch(v -> v.inject(params, rp)) &&
                cConstraints.stream().filter(c -> !(c instanceof CObjective))
                        .allMatch(c -> c.inject(params, rp)) &&
                views.stream().allMatch(v -> v.beforeSolve(rp)) &&
                (!o.isPresent() || o.isPresent() && o.get().inject(params, rp));
    }

    private ReconfigurationProblem buildRP() throws SchedulerException {
        //Build the RP. As VM state management is not possible
        //We extract VM-state related constraints first.
        //For other constraint, we just create the right choco constraint
        Set<VM> toRun = new HashSet<>();
        Set<VM> toForge = new HashSet<>();
        Set<VM> toKill = new HashSet<>();
        Set<VM> toSleep = new HashSet<>();

        cConstraints = new ArrayList<>();

        for (SatConstraint cstr : cstrs) {
            checkNodesExistence(origin, cstr.getInvolvedNodes());

            //We cannot check for VMs that are going to the ready state
            //as they are not forced to be a part of the initial model
            //(when they will be forged)
            if (!(cstrs instanceof Ready)) {
                checkUnknownVMsInMapping(origin, cstr.getInvolvedVMs());
            }

            if (cstr instanceof Running) {
                toRun.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Sleeping) {
                toSleep.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Ready) {
                checkUnknownVMsInMapping(origin, cstr.getInvolvedVMs());
                toForge.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Killed) {
                checkUnknownVMsInMapping(origin, cstr.getInvolvedVMs());
                toKill.addAll(cstr.getInvolvedVMs());
            }

            cConstraints.add(build(cstr));
        }
        cConstraints.add(build(obj));

        views = makeViews();

        DefaultReconfigurationProblemBuilder rpb = new DefaultReconfigurationProblemBuilder(origin)
                .setNextVMsStates(toForge, toRun, toSleep, toKill)
                .setParams(params);

        if (params.doRepair()) {
            Set<VM> toManage = new HashSet<>();
            cConstraints.forEach(c -> toManage.addAll(c.getMisPlacedVMs(instance)));
            views.forEach(v -> toManage.addAll(v.getMisPlacedVMs(instance)));
            rpb.setManageableVMs(toManage);
        }

        //The core views have been instantiated and available through rp.getViews()
        //Set the maximum duration
        ReconfigurationProblem p = rpb.build();
        try {
            p.getEnd().updateUpperBound(params.getMaxEnd(), Cause.Null);
        } catch (ContradictionException e) {
            p.getLogger().error("Unable to restrict the maximum plan duration to " + params.getMaxEnd(), e);
            return null;
        }
        return p;
    }

    private List<ChocoView> makeViews() throws SchedulerException {
        List<ChocoView> l = new ArrayList<>();
        ChocoMapper mapper = params.getMapper();
        origin.getViews().stream().filter(v -> mapper.viewHasMapping(v.getClass())).forEach(v -> l.add(mapper.get(v)));
        return l;
    }

    /**
     * Build a sat constraint
     *
     * @param cstr the model-side constraint
     * @return the solver-side constraint
     * @throws SchedulerException if the process failed
     */
    private ChocoConstraint build(Constraint cstr) throws SchedulerException {
        ChocoMapper mapper = params.getMapper();
        ChocoConstraint cc = mapper.get(cstr);
        if (cc == null) {
            throw new SchedulerModelingException(origin, "No implementation mapped to '" + cstr.getClass().getSimpleName() + "'");
        }
        return cc;
    }

    private static void checkUnknownVMsInMapping(Model m, Collection<VM> vms) throws SchedulerException {
        for (VM v : vms) {
            //This loop prevent from a useless allocation of memory when there is no issue
            if (!m.getMapping().contains(v)) {
                Set<VM> unknown = new HashSet<>(vms);
                unknown.removeAll(m.getMapping().getAllVMs());
                throw new SchedulerModelingException(m, "Unknown VMs: " + unknown);
            }
        }
    }

    /**
     * Check for the existence of nodes in a model
     *
     * @param mo the model to check
     * @param ns the nodes to check
     * @throws SchedulerModelingException if at least one of the given nodes is not in the RP.
     */
    private static void checkNodesExistence(Model mo, Collection<Node> ns) throws SchedulerModelingException {
        for (Node node : ns) {
            if (!mo.getMapping().contains(node)) {
                throw new SchedulerModelingException(mo, "Unknown node '" + node + "'");
            }
        }
    }

    /**
     * Get the statistics about the solving process.
     *
     * @return the statistics
     */
    public SingleRunnerStatistics getStatistics() {
        if (rp != null) {
            Measures m = rp.getSolver().getMeasures();
            stats.setMetrics(new Metrics(m));
          stats.setCompleted(m.getSearchState().equals(SearchState.TERMINATED)
                  || m.getSearchState().equals(SearchState.NEW)
          );
        }
        return stats;
    }

    /**
     * Stop the solver.
     */
    public void stop() {
        if (rp == null) {
            return;
        }
        rp.stop();
    }

}
