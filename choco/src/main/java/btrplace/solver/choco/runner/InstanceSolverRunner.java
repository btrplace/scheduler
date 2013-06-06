/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.runner;

import btrplace.model.Instance;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.*;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanChecker;
import btrplace.plan.ReconfigurationPlanCheckerException;
import btrplace.solver.SolverException;
import btrplace.solver.choco.*;
import btrplace.solver.choco.constraint.ChocoConstraint;
import btrplace.solver.choco.constraint.ChocoConstraintBuilder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solution;
import choco.kernel.solver.search.measure.IMeasures;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author Fabien Hermenier
 */
public class InstanceSolverRunner implements Callable<InstanceResult> {

    private ChocoReconfigurationAlgorithmParams params;

    private ReconfigurationProblem rp;

    private Collection<SatConstraint> cstrs;

    private OptimizationConstraint obj;

    private Model origin;

    private long coreRPDuration;

    private long speRPDuration;

    /**
     * Make a new runner.
     *
     * @param ps the parameters for the solving process
     * @param i  the instance to solve
     */
    public InstanceSolverRunner(ChocoReconfigurationAlgorithmParams ps, Instance i) {
        cstrs = i.getConstraints();
        obj = i.getOptimizationConstraint();
        origin = i.getModel();
        params = ps;
    }

    @Override
    public InstanceResult call() throws SolverException {
        rp = null;
        coreRPDuration = -System.currentTimeMillis();
        //Build the RP. As VM state management is not possible
        //We extract VM-state related constraints first.
        //For other constraint, we just create the right choco constraint
        Set<VM> toRun = new HashSet<>();
        Set<VM> toForge = new HashSet<>();
        Set<VM> toKill = new HashSet<>();
        Set<VM> toSleep = new HashSet<>();

        List<ChocoConstraint> cConstraints = new ArrayList<>();
        for (SatConstraint cstr : cstrs) {
            checkNodesExistence(origin, cstr.getInvolvedNodes());

            //We cannot check for VMs that are going to the ready state
            //as they are not forced to be a part of the initial model
            //(when they will be forged)
            if (!(cstrs instanceof Ready)) {
                checkUnkownVMsInMapping(origin, cstr.getInvolvedVMs());
            }

            if (cstr instanceof Running) {
                toRun.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Sleeping) {
                toSleep.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Ready) {
                checkUnkownVMsInMapping(origin, cstr.getInvolvedVMs());
                toForge.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Killed) {
                checkUnkownVMsInMapping(origin, cstr.getInvolvedVMs());
                toKill.addAll(cstr.getInvolvedVMs());
            }

            ChocoConstraintBuilder ccstrb = params.getConstraintMapper().getBuilder(cstr.getClass());
            if (ccstrb == null) {
                throw new SolverException(origin, "Unable to map constraint '" + cstr.getClass().getSimpleName() + "'");
            }
            ChocoConstraint ccstr = ccstrb.build(cstr);
            if (ccstr == null) {
                throw new SolverException(origin, "Error while mapping the constraint '"
                        + cstr.getClass().getSimpleName() + "'");
            }

            cConstraints.add(ccstr);
        }

        //Make the optimization constraint
        ChocoConstraintBuilder ccstrb = params.getConstraintMapper().getBuilder(obj.getClass());
        if (ccstrb == null) {
            throw new SolverException(origin, "Unable to map constraint '" + obj.getClass().getSimpleName() + "'");
        }
        ChocoConstraint cObj = ccstrb.build(obj);
        if (cObj == null) {
            throw new SolverException(origin, "Error while mapping the constraint '"
                    + obj.getClass().getSimpleName() + "'");
        }


        //Make the core-RP
        DefaultReconfigurationProblemBuilder rpb = new DefaultReconfigurationProblemBuilder(origin)
                .setNextVMsStates(toForge, toRun, toSleep, toKill)
                .setViewMapper(params.getViewMapper())
                .setDurationEvaluatators(params.getDurationEvaluators());
        if (params.doRepair()) {
            Set<VM> toManage = new HashSet<>();
            for (ChocoConstraint cstr : cConstraints) {
                toManage.addAll(cstr.getMisPlacedVMs(origin));
            }
            toManage.addAll(cObj.getMisPlacedVMs(origin));
            rpb.setManageableVMs(toManage);
        }
        if (params.areVariablesLabelled()) {
            rpb.labelVariables();
        }
        rp = rpb.build();

        //Set the maximum duration
        try {
            rp.getEnd().setSup(params.getMaxEnd());
        } catch (ContradictionException e) {
            rp.getLogger().error("Unable to restrict the maximum plan duration to {}", params.getMaxEnd());
            return null;
        }
        coreRPDuration += System.currentTimeMillis();

        //Customize with the constraints
        speRPDuration = -System.currentTimeMillis();
        for (ChocoConstraint ccstr : cConstraints) {
            if (!ccstr.inject(rp)) {
                return new InstanceResult(null, makeStatistics());
            }
        }

        //The objective
        cObj.inject(rp);
        speRPDuration += System.currentTimeMillis();
        rp.getLogger().debug("{} ms to build the core-RP + {} ms to tune it", coreRPDuration, speRPDuration);

        rp.getLogger().debug("{} nodes; {} VMs; {} constraints", rp.getNodes().length, rp.getVMs().length, cstrs.size());
        rp.getLogger().debug("optimize: {}; timeLimit: {}; manageableVMs: {}", params.doOptimize(), params.getTimeLimit(), rp.getManageableVMs().size());

        stateVerbosity();

        //The actual solving process
        ReconfigurationPlan p = rp.solve(params.getTimeLimit(), params.doOptimize());
        if (p == null) {
            return new InstanceResult(null, makeStatistics());
        }
        checkSatisfaction2(p, cstrs);
        return new InstanceResult(p, makeStatistics());
    }

    private void stateVerbosity() {
        if (params.getVerbosity() <= 0) {
            ChocoLogging.setVerbosity(Verbosity.SILENT);
            params.labelVariables(false);
        } else {
            params.labelVariables(true);
            ChocoLogging.setVerbosity(Verbosity.SOLUTION);
            if (params.getVerbosity() == 2) {
                ChocoLogging.setVerbosity(Verbosity.SEARCH);
                ChocoLogging.setLoggingMaxDepth(Integer.MAX_VALUE);
            } else if (params.getVerbosity() > 2) {
                ChocoLogging.setVerbosity(Verbosity.FINEST);
            }
        }
    }

    private void checkSatisfaction2(ReconfigurationPlan p, Collection<SatConstraint> cstrs) throws SolverException {
        ReconfigurationPlanChecker chk = new ReconfigurationPlanChecker();
        for (SatConstraint c : cstrs) {
            chk.addChecker(c.getChecker());
        }
        try {
            chk.check(p);
        } catch (ReconfigurationPlanCheckerException ex) {
            throw new SolverException(p.getOrigin(), ex.getMessage(), ex);
        }
    }

    private void checkUnkownVMsInMapping(Model m, Collection<VM> vms) throws SolverException {
        if (!m.getMapping().getAllVMs().containsAll(vms)) {
            Set<VM> unknown = new HashSet<>(vms);
            unknown.removeAll(m.getMapping().getAllVMs());
            throw new SolverException(m, "Unknown VMs: " + unknown);
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
            if (!mo.getMapping().getAllNodes().contains(node)) {
                throw new SolverException(mo, "Unknown node '" + node + "'");
            }
        }
    }

    private SolvingStatistics makeStatistics() {
        if (rp == null) {
            return new SolvingStatistics(0, 0, 0, params.doOptimize(), params.getTimeLimit(), 0, 0, 0, 0, false, 0, 0);
        }
        SolvingStatistics st = new SolvingStatistics(
                rp.getNodes().length,
                rp.getVMs().length,
                cstrs.size(),
                params.doOptimize(),
                params.getTimeLimit(),
                rp.getManageableVMs().size(),
                rp.getSolver().getTimeCount(),
                rp.getSolver().getNodeCount(),
                rp.getSolver().getBackTrackCount(),
                rp.getSolver().isEncounteredLimit(),
                coreRPDuration,
                speRPDuration);

        if (rp.getSolver().getSearchStrategy() != null) {
            for (Solution s : rp.getSolver().getSearchStrategy().getStoredSolutions()) {
                IMeasures m = s.getMeasures();
                SolutionStatistics sol;
                if (m.getObjectiveValue() != null) {
                    sol = new SolutionStatistics(m.getNodeCount(),
                            m.getBackTrackCount(),
                            m.getTimeCount(),
                            m.getObjectiveValue().intValue());
                } else {
                    sol = new SolutionStatistics(m.getNodeCount(),
                            m.getBackTrackCount(),
                            m.getTimeCount());
                }
                st.addSolution(sol);
            }
        }
        return st;
    }
}
