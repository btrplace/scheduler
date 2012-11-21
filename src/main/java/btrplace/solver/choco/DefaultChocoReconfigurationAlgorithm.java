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
import btrplace.model.constraint.Destroyed;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.Sleeping;
import btrplace.model.constraint.Waiting;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.SolverException;
import choco.kernel.solver.Solution;
import choco.kernel.solver.search.measure.IMeasures;

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

    private ReconfigurationProblem rp;

    private DurationEvaluators durationEvaluators;

    /**
     * Make a new algorithm.
     */
    public DefaultChocoReconfigurationAlgorithm() {

        cstrMapper = new SatConstraintMapper();
        durationEvaluators = new DurationEvaluators();
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
    public ReconfigurationPlan solve(Model i) throws SolverException {
        rp = null;
        //Build the RP. As VM state management is not possible
        //We extract VM-state related constraints first.
        //For other constraint, we just create the right choco constraint
        Set<UUID> toRun = new HashSet<UUID>();
        Set<UUID> toWait = new HashSet<UUID>();
        Set<UUID> toDestroy = new HashSet<UUID>();
        Set<UUID> toSleep = new HashSet<UUID>();

        List<ChocoSatConstraint> cConstraints = new ArrayList<ChocoSatConstraint>();
        for (SatConstraint cstr : i.getConstraints()) {
            if (cstr instanceof Running) {
                toRun.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Sleeping) {
                toSleep.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Waiting) {
                toWait.addAll(cstr.getInvolvedVMs());
            } else if (cstr instanceof Destroyed) {
                toDestroy.addAll(cstr.getInvolvedVMs());
            } else {
                ChocoConstraintBuilder ccstrb = cstrMapper.getBuilder(cstr.getClass());
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
        rp = new DefaultReconfigurationProblem(i, durationEvaluators, toWait, toRun, toSleep, toDestroy);

        //Customize with the constraints
        for (ChocoSatConstraint ccstr : cConstraints) {
            ccstr.inject(rp);
        }

        //The objective

        //The heuristics

        //Let's rock
        if (timeLimit > 0) {
            rp.getSolver().setTimeLimit(timeLimit);
        }
        Boolean ret = rp.getSolver().solve();
        if (Boolean.TRUE.equals(ret)) {
            return rp.extractSolution();
        } else if (Boolean.FALSE.equals(ret)) {
            return null;
        } else {
            throw new SolverException(i, "Unable to state about the feasibility of the problem");
        }
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
