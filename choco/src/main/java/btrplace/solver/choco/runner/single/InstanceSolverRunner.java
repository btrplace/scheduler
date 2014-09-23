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

package btrplace.solver.choco.runner.single;

import btrplace.model.Instance;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanChecker;
import btrplace.plan.ReconfigurationPlanCheckerException;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.Parameters;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.constraint.ChocoConstraint;
import btrplace.solver.choco.constraint.ChocoConstraintBuilder;
import btrplace.solver.choco.runner.InstanceResult;
import btrplace.solver.choco.runner.SolutionStatistics;
import solver.Cause;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.IMonitorSolution;
import solver.search.loop.monitors.SMF;
import solver.search.measure.IMeasures;

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

    private long coreRPDuration;

    private long speRPDuration;

    private long start;

    private List<SolutionStatistics> measures;

    /**
     * Choco version of the constraints.
     */
    private List<ChocoConstraint> cConstraints;

    /**
     * Make a new runner.
     *
     * @param ps the parameters for the solving process
     * @param i  the instance to solve
     */
    public InstanceSolverRunner(Parameters ps, Instance i) {
        cstrs = i.getSatConstraints();
        obj = i.getOptConstraint();
        origin = i.getModel();
        params = ps;
    }

    @Override
    public InstanceResult call() throws SolverException {
        rp = null;
        start = System.currentTimeMillis();
        measures = new ArrayList<>();

        //Build the core problem
        coreRPDuration = -System.currentTimeMillis();
        rp = buildRP();
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
        if (!injectConstraints()) {
            return new InstanceResult(null, makeStatistics());
        }
        speRPDuration += System.currentTimeMillis();

        //statistics
        rp.getLogger().debug("{} ms to build the core-RP + {} ms to tune it", coreRPDuration, speRPDuration);
        rp.getLogger().debug("{} nodes; {} VMs; {} constraints", rp.getNodes().length, rp.getVMs().length, cstrs.size());
        rp.getLogger().debug("optimize: {}; timeLimit: {}; manageableVMs: {}", params.doOptimize(), params.getTimeLimit(), rp.getManageableVMs().size());

        //The solution monitor to store the measures at each solution
        rp.getSolver().getSearchLoop().plugSearchMonitor(new IMonitorSolution() {
            @Override
            public void onSolution() {
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
            }
        });

        //State the logging level for the solver
        SMF.log(rp.getSolver(), params.getVerbosity() >= 2, params.getVerbosity() >= 3);

        //The actual solving process
        ReconfigurationPlan p = rp.solve(params.getTimeLimit(), params.doOptimize());
        return new InstanceResult(p, makeStatistics());
    }

    private ReconfigurationProblem buildRP() throws SolverException {
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

            cConstraints.add(buildSatConstraint(cstr));
        }

        cConstraints.add(buildOptConstraint());

        DefaultReconfigurationProblemBuilder rpb = new DefaultReconfigurationProblemBuilder(origin)
                .setNextVMsStates(toForge, toRun, toSleep, toKill)
                .setParams(params);

        if (params.doRepair()) {
            Set<VM> toManage = new HashSet<>();
            for (ChocoConstraint cstr : cConstraints) {
                toManage.addAll(cstr.getMisPlacedVMs(origin));
            }
            rpb.setManageableVMs(toManage);
        }

        return rpb.build();
    }

    /**
     * Inject the constraints inside the problem.
     */
    private boolean injectConstraints() throws SolverException {
        try {
            for (ChocoConstraint cc : cConstraints) {
                if (!cc.inject(rp)) {
                    return false;
                }
            }
        } catch (UnsupportedOperationException ex) {
            return false;
        }
        return true;
    }

    /**
     * Build a sat constraint
     *
     * @param cstr the model-side constraint
     * @return the solver-side constraint
     * @throws SolverException if the process failed
     */
    private ChocoConstraint buildSatConstraint(SatConstraint cstr) throws SolverException {
        ChocoConstraintBuilder ccBuilder = params.getConstraintMapper().getBuilder(cstr.getClass());
        if (ccBuilder == null) {
            throw new SolverException(origin, "Unable to map constraint '" + cstr.getClass().getSimpleName() + "'");
        }
        ChocoConstraint cc = ccBuilder.build(cstr);
        if (cc == null) {
            throw new SolverException(origin, "Error while mapping the constraint '"
                    + cstr.getClass().getSimpleName() + "'");
        }
        return cc;
    }

    /**
     * Make the optimization constraint
     */
    private ChocoConstraint buildOptConstraint() throws SolverException {
        ChocoConstraintBuilder ccBuilder = params.getConstraintMapper().getBuilder(obj.getClass());
        if (ccBuilder == null) {
            throw new SolverException(origin, "Unable to map constraint '" + obj.getClass().getSimpleName() + "'");
        }
        ChocoConstraint cObj = ccBuilder.build(obj);
        if (cObj == null) {
            throw new SolverException(origin, "Error while mapping the constraint '"
                    + obj.getClass().getSimpleName() + "'");
        }
        return cObj;
    }

    private void checkSatisfaction2(ReconfigurationPlan p, Collection<SatConstraint> cs) throws SolverException {
        ReconfigurationPlanChecker chk = new ReconfigurationPlanChecker();
        for (SatConstraint c : cs) {
            chk.addChecker(c.getChecker());
        }
        try {
            chk.check(p);
        } catch (ReconfigurationPlanCheckerException ex) {
            throw new SolverException(p.getOrigin(), ex.getMessage(), ex);
        }
    }

    private void checkUnknownVMsInMapping(Model m, Collection<VM> vms) throws SolverException {
        for (VM v : vms) {
            //This loop prevent from a useless allocation of memory when there is no issue
            if (!m.getMapping().contains(v)) {
                Set<VM> unknown = new HashSet<>(vms);
                unknown.removeAll(m.getMapping().getAllVMs());
                throw new SolverException(m, "Unknown VMs: " + unknown);
            }
        }
    }

    /**
     * Check for the existence of nodes in a model
     *
     * @param mo the model to check
     * @param ns the nodes to check
     * @throws SolverException if at least one of the given nodes is not in the RP.
     */
    private void checkNodesExistence(Model mo, Collection<Node> ns) throws SolverException {
        for (Node node : ns) {
            if (!mo.getMapping().contains(node)) {
                throw new SolverException(mo, "Unknown node '" + node + "'");
            }
        }
    }

    private SingleRunnerStatistics makeStatistics() {
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
                params.getTimeLimit() <= m2.getTimeCount(),
                coreRPDuration,
                speRPDuration);

        for (SolutionStatistics m : measures) {
            st.addSolution(m);
        }
        return st;
    }
}
