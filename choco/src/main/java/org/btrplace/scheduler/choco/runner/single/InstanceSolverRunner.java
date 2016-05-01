/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
    public InstanceResult call() throws SchedulerException {
        stats = new SingleRunnerStatistics(params, instance, System.currentTimeMillis());
        rp = null;

        //Build the core problem
        long d = -System.currentTimeMillis();
        rp = buildRP();
        d += System.currentTimeMillis();
        stats.setCoreBuildDuration(d);
        stats.setNbManagedVMs(rp.getManageableVMs().size());

        //Customize the core problem
        d = -System.currentTimeMillis();
        if (!specialise()) {
            stats.setSpecialisationDuration(d);
            return new InstanceResult(null, getStatistics());
        }
        d += System.currentTimeMillis();
        stats.setSpecialisationDuration(d);

        //statistics
        stats.setMeasures(rp.getSolver().getMeasures().duplicate());
        rp.getLogger().debug(stats.toString());

        //The solution monitor to store the measures at each solution
        rp.getSolver().plugMonitor((IMonitorSolution) () -> {
            Solution solution = new Solution();
            solution.record(rp.getSolver());
            IMeasures m = rp.getSolver().getMeasures().duplicate();

            ReconfigurationPlan plan = rp.buildReconfigurationPlan(solution, origin);
            views.forEach(v -> v.insertActions(rp, solution, plan));

            SolutionStatistics sol = new SolutionStatistics(m, plan);
            stats.addSolution(sol);
        });

        setVerbosity();

        //The actual solving process
        ReconfigurationPlan plan = rp.solve(params.getTimeLimit(), params.doOptimize());
        List<SolutionStatistics> sols = stats.getSolutions();
        if (plan == null) {
            return new InstanceResult(null, getStatistics());
        }
        return new InstanceResult(sols.get(sols.size() - 1).getReconfigurationPlan(), getStatistics());
    }


    private void setVerbosity() {
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
    }

    private boolean specialise() {

        //Resolve the view dependencies, add them and inject them
        views = ChocoViews.resolveDependencies(origin, views, rp.getViews());
        views.forEach(rp::addView);
        return views.stream().allMatch(v -> v.inject(params, rp)) &&
                cConstraints.stream().allMatch(c -> c.inject(params, rp)) &&
                views.stream().allMatch(v -> v.beforeSolve(rp));

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
            throw new SchedulerException(origin, "No implementation mapped to '" + cstr.getClass().getSimpleName() + "'");
        }
        return cc;
    }

    private static void checkUnknownVMsInMapping(Model m, Collection<VM> vms) throws SchedulerException {
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
    private static void checkNodesExistence(Model mo, Collection<Node> ns) throws SchedulerException {
        for (Node node : ns) {
            if (!mo.getMapping().contains(node)) {
                throw new SchedulerException(mo, "Unknown node '" + node + "'");
            }
        }
    }

    /**
     * Get the statistics about the solving process.
     *
     * @return the statistics
     */
    public SingleRunnerStatistics getStatistics() {
        IMeasures m = rp.getSolver().getMeasures().duplicate();
        stats.setMeasures(m);
        stats.setCompleted(!rp.getSolver().hasReachedLimit());
        return stats;
    }
}
