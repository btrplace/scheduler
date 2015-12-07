/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.scheduler.choco.runner.single;

import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.*;
import org.btrplace.model.view.ModelView;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.DefaultReconfigurationProblemBuilder;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.constraint.ChocoConstraint;
import org.btrplace.scheduler.choco.constraint.ChocoMapper;
import org.btrplace.scheduler.choco.runner.InstanceResult;
import org.btrplace.scheduler.choco.runner.SolutionStatistics;
import org.btrplace.scheduler.choco.view.ChocoView;
import org.btrplace.scheduler.choco.view.ChocoViews;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.solution.AllSolutionsRecorder;
import org.chocosolver.solver.search.solution.ISolutionRecorder;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.trace.Chatterbox;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * A basic solver that solve a whole instance.
 * Use {@link #call()} to compute a solution
 *
 * @author Fabien Hermenier
 */
public class InstanceSolverRunner implements Callable<InstanceResult> {

    private Parameters params;

    private ReconfigurationProblem rp;

    private Collection<SatConstraint> cstrs;

    private OptConstraint obj;

    private Model origin;

    private Instance instance;

    private long coreRPDuration;

    private long speRPDuration;

    private long start;

    private List<SolutionStatistics> measures;
    private ISolutionRecorder solutions;

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
    public InstanceResult call() throws SchedulerException {
        rp = null;
        start = System.currentTimeMillis();
        measures = new ArrayList<>();
        //Build the core problem
        coreRPDuration = -System.currentTimeMillis();
        rp = buildRP();
        //The core views have been instantiated and available through rp.getViews()

        //Set the maximum duration
        try {
            rp.getEnd().updateUpperBound(params.getMaxEnd(), Cause.Null);
        } catch (ContradictionException e) {
            rp.getLogger().error("Unable to restrict the maximum plan duration to {}", params.getMaxEnd());
            return null;
        }
        coreRPDuration += System.currentTimeMillis();

        //Customize the core problem
        speRPDuration = -System.currentTimeMillis();

        //Resolve the view dependencies, add them and inject them
        views = ChocoViews.resolveDependencies(origin, views, rp.getViews());
        views.forEach(rp::addView);
        if (!views.stream().allMatch((v) -> v.inject(params, rp))) {
            return new InstanceResult(null, getStatistics());
        }


        if (!cConstraints.stream().allMatch((c) -> c.inject(params, rp))) {
            return new InstanceResult(null, getStatistics());
        }

        if (!views.stream().allMatch((v) -> v.beforeSolve(rp))) {
            return new InstanceResult(null, getStatistics());
        }

        speRPDuration += System.currentTimeMillis();

        //statistics
        rp.getLogger().debug("{} ms to build the core-RP + {} ms to tune it", coreRPDuration, speRPDuration);
        rp.getLogger().debug("{} nodes; {} VMs; {} constraints", rp.getNodes().length, rp.getVMs().length, cstrs.size());
        rp.getLogger().debug("optimize: {}; timeLimit: {}; manageableVMs: {}", params.doOptimize(), params.getTimeLimit(), rp.getManageableVMs().size());

        //The solution monitor to store the measures at each solution
        solutions = new AllSolutionsRecorder(rp.getSolver());
        rp.getSolver().plugMonitor((IMonitorSolution) () -> {
            IMeasures m = rp.getSolver().getMeasures();
            SolutionStatistics sol;
            if (m.hasObjective()) {
                sol = new SolutionStatistics(m.getNodeCount(),
                        m.getBackTrackCount(),
                        (long) (m.getTimeCount() * 1000),
                        m.getBestSolutionValue().intValue());
            } else {
                sol = new SolutionStatistics(m.getNodeCount(),
                        m.getBackTrackCount(),
                        (long) (m.getTimeCount() * 1000));
            }
            measures.add(sol);
        });

        if (params.getVerbosity() >=1) {
            Chatterbox.showSolutions(rp.getSolver());
        }
        if (params.getVerbosity() >= 2) {
            //every second
            Chatterbox.showStatisticsDuringResolution(rp.getSolver(), 1000);
        }
        if (params.getVerbosity() >= 3) {
            Chatterbox.showDecisions(rp.getSolver());
        }
        if (params.getVerbosity() >= 4) {
            Chatterbox.showContradiction(rp.getSolver());
        }
        //The actual solving process
        ReconfigurationPlan p = rp.solve(params.getTimeLimit(), params.doOptimize());

        if (p != null) {
            Solution s = solutions.getLastSolution();
            views.forEach(v -> v.insertActions(rp, s, p));
        }
        return new InstanceResult(p, getStatistics());
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

        return rpb.build();
    }

    private List<ChocoView> makeViews() throws SchedulerException {
        List<ChocoView> l = new ArrayList<>();
        ChocoMapper mapper = params.getMapper();
        for (ModelView v : origin.getViews()) {
            ChocoView cv = mapper.get(v);
        }
        origin.getViews().stream().filter(v -> mapper.viewHasMapping(v.getClass())).forEach(v -> l.add(mapper.get(v)));
        return l;
        //List<String> base = new ArrayList<>();
        /*for (SolverViewBuilder vb : params.getSolverViews()) {
            base.add(vb.getKey());
        }*/
        //return ChocoViews.resolveDependencies(origin, l, base);
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
            throw new SchedulerException(origin, "No implementation mapped to '" + cstr.getClass().getSimpleName() + "'");
        }
        return cc;
    }

    private void checkUnknownVMsInMapping(Model m, Collection<VM> vms) throws SchedulerException {
        for (VM v : vms) {
            //This loop prevent from a useless allocation of memory when there is no issue
            if (!m.getMapping().contains(v)) {
                Set<VM> unknown = new HashSet<>(vms);
                unknown.removeAll(m.getMapping().getAllVMs());
                throw new SchedulerException(m, "Unknown VMs: " + unknown);
            }
        }
    }

    /**
     * Check for the existence of nodes in a model
     *
     * @param mo the model to check
     * @param ns the nodes to check
     * @throws org.btrplace.scheduler.SchedulerException if at least one of the given nodes is not in the RP.
     */
    private void checkNodesExistence(Model mo, Collection<Node> ns) throws SchedulerException {
        for (Node node : ns) {
            if (!mo.getMapping().contains(node)) {
                throw new SchedulerException(mo, "Unknown node '" + node + "'");
            }
        }
    }

    public SingleRunnerStatistics getStatistics() throws SchedulerException {
        if (rp == null) {
            return new SingleRunnerStatistics(params, 0, 0, 0, 0, 0, 0, 0, 0, false, 0, 0);
        }

        IMeasures m2 = rp.getSolver().getMeasures();
        SingleRunnerStatistics st = new SingleRunnerStatistics(
                params,
                rp.getNodes().length,
                rp.getVMs().length,
                cstrs.size(),
                rp.getManageableVMs().size(),
                start,
                (long) (m2.getTimeCount() * 1000),
                m2.getNodeCount(),
                m2.getBackTrackCount(),
                rp.getSolver().hasReachedLimit(), //assumed timeout is the only limit
                coreRPDuration,
                speRPDuration);
        int i = 0;
        //Merge the statistics with the solution.
        for (SolutionStatistics m : measures) {
            m.setReconfigurationPlan(rp.buildReconfigurationPlan(solutions.getSolutions().get(i), rp.getSourceModel()));
            st.addSolution(m);
            i++;
        }
        return st;
    }
}
