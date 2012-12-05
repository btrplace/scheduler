/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace.solver.choco;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.constraint.Killed;
import btrplace.model.constraint.Ready;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.Sleeping;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.constraint.SatConstraintMapper;
import btrplace.solver.choco.constraint.TaskSchedulerBuilder;
import btrplace.solver.choco.objective.minMTTR.MinMTTR;
import choco.cp.solver.CPSolver;
import choco.kernel.solver.Solution;
import choco.kernel.solver.search.ISolutionPool;
import choco.kernel.solver.search.SolutionPoolFactory;
import choco.kernel.solver.search.measure.IMeasures;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.*;

/**
 * Default implementation of {@link ChocoReconfigurationAlgorithm}.
 *
 * @author Fabien Hermenier
 */
public class DefaultChocoReconfigurationAlgorithm implements ChocoReconfigurationAlgorithm {

    private SatConstraintMapper cstrMapper;

    private boolean optimize = false;

    private int timeLimit = 5;

    private boolean repair = false;

    private boolean useLabels = false;

    private ReconfigurationProblem rp;

    private DurationEvaluators durationEvaluators;

    private ReconfigurationObjective obj;

    /**
     * Make a new algorithm.
     */
    public DefaultChocoReconfigurationAlgorithm() {

        cstrMapper = new SatConstraintMapper();
        durationEvaluators = new DurationEvaluators();

        //Default objective
        obj = new MinMTTR();
    }

    @Override
    public void doOptimize(boolean b) {
        this.optimize = b;
    }

    @Override
    public boolean doOptimize() {
        return this.optimize;
    }

    @Override
    public void setTimeLimit(int t) {
        timeLimit = t;
    }

    @Override
    public int getTimeLimit() {
        return timeLimit;
    }

    @Override
    public void repair(boolean b) {
        repair = b;
    }

    @Override
    public boolean repair() {
        return repair;
    }


    @Override
    public void labelVariables(boolean b) {
        useLabels = b;
    }

    @Override
    public boolean areVariablesLabelled() {
        return useLabels;
    }

    @Override
    public ReconfigurationPlan solve(Model i, Collection<SatConstraint> cstrs) throws SolverException {
        rp = null;
        //Build the RP. As VM state management is not possible
        //We extract VM-state related constraints first.
        //For other constraint, we just create the right choco constraint
        Set<UUID> toRun = new HashSet<UUID>();
        Set<UUID> toForge = new HashSet<UUID>();
        Set<UUID> toKill = new HashSet<UUID>();
        Set<UUID> toSleep = new HashSet<UUID>();

        List<ChocoSatConstraint> cConstraints = new ArrayList<ChocoSatConstraint>();
        for (SatConstraint cstr : cstrs) {
            if (cstr instanceof Running) {
                toRun.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Sleeping) {
                toSleep.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Ready) {
                toForge.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Killed) {
                toKill.addAll(cstr.getInvolvedVMs());
            } else {
                ChocoSatConstraintBuilder ccstrb = cstrMapper.getBuilder(cstr.getClass());
                if (ccstrb == null) {
                    throw new SolverException(i, "Unable to map constraint '" + cstr.getClass().getSimpleName() + "'");
                }
                ChocoSatConstraint ccstr = ccstrb.build(cstr);
                if (ccstr == null) {
                    throw new SolverException(i, "Error while mapping the constraint '" + cstr.getClass().getSimpleName() + "'");
                } else {
                    cConstraints.add(ccstr);
                }
            }
        }

        //Make the core-RP
        DefaultReconfigurationProblemBuilder rpb = new DefaultReconfigurationProblemBuilder(i)
                .setNextVMsStates(toForge, toRun, toSleep, toKill)
                .setDurationEvaluatators(durationEvaluators);

        if (repair) {
            Set<UUID> toManage = new HashSet<UUID>();
            for (ChocoSatConstraint cstr : cConstraints) {
                toManage.addAll(cstr.getMisPlacedVMs(i));
            }
            rpb.setManageableVMs(toManage);
        }
        if (useLabels) {
            rpb.labelVariables();
        }
        rp = rpb.build();

        TaskSchedulerBuilder.begin(rp);
        //Customize with the constraints
        for (ChocoSatConstraint ccstr : cConstraints) {
            ccstr.inject(rp);
        }

        //The objective
        obj.inject(rp);


        addContinuousResourceCapacities();


        TaskSchedulerBuilder.getInstance().commitConstraint();


        CPSolver s = rp.getSolver();
        s.generateSearchStrategy();
        ISolutionPool sp = SolutionPoolFactory.makeInfiniteSolutionPool(s.getSearchStrategy());
        s.getSearchStrategy().setSolutionPool(sp);

        //Let's rock
        if (timeLimit > 0) {
            s.setTimeLimit(timeLimit);
        }
        s.setFirstSolution(!optimize);
        s.launch();
        Boolean ret = s.isFeasible();
        if (Boolean.TRUE.equals(ret)) {
            return rp.extractSolution();
        } else if (Boolean.FALSE.equals(ret)) {
            return null;
        } else {
            throw new SolverException(i, "Unable to state about the feasibility of the problem");
        }
    }

    private void addContinuousResourceCapacities() {
        IntDomainVar[] iUse = new IntDomainVar[rp.getVMs().length];
        int[] cUse = new int[rp.getVMs().length];
        for (int j = 0; j < rp.getVMs().length; j++) {
            iUse[j] = rp.getSolver().makeConstantIntVar(1);
            cUse[j] = 1;
        }

        TaskSchedulerBuilder.getInstance().add(rp.getVMsCountOnNodes(), cUse, iUse);
    }

    @Override
    public SatConstraintMapper getSatConstraintMapper() {
        return cstrMapper;
    }

    @Override
    public DurationEvaluators getDurationEvaluators() {
        return durationEvaluators;
    }

    @Override
    public ReconfigurationObjective getObjective() {
        return obj;
    }

    @Override
    public void setObjective(ReconfigurationObjective o) {
        obj = o;
    }

    @Override
    public SolvingStatistics getSolvingStatistics() {
        if (rp == null) {
            return new SolvingStatistics(0, 0, 0, false);
        }
        SolvingStatistics st = new SolvingStatistics(
                rp.getSolver().getTimeCount(),
                rp.getSolver().getNodeCount(),
                rp.getSolver().getBackTrackCount(),
                rp.getSolver().isEncounteredLimit());

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
        return st;
    }
}
