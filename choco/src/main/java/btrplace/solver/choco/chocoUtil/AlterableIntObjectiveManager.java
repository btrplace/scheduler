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

package btrplace.solver.choco.chocoUtil;

import btrplace.solver.choco.ObjectiveAlterer;
import btrplace.solver.choco.ReconfigurationProblem;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.objective.ObjectiveManager;
import solver.variables.IntVar;

/**
 * @author Fabien Hermenier
 */
public class AlterableIntObjectiveManager extends ObjectiveManager {

    final IntVar objective;
    final boolean strict;
    int bestKnownUpperBound;
    int bestKnownLowerBound;

    private ObjectiveAlterer alterator;

    private ReconfigurationProblem rp;

    /**
     * Creates an optimization manager
     * Enables to cut "worse" solutions
     *
     * @param objective variable (represent the value of a solution)
     * @param policy    SATISFACTION / MINIMIZATION / MAXIMIZATION
     * @param solver
     * @param strict    enables to find same value solutions when set to false
     */
    public AlterableIntObjectiveManager(ReconfigurationProblem rp, final IntVar objective, ResolutionPolicy policy, Solver solver, boolean strict) {
        super(policy, solver.getMeasures());
        this.objective = objective;
        this.rp = rp;
        if (policy != ResolutionPolicy.SATISFACTION) {
            this.bestKnownLowerBound = objective.getLB();
            this.bestKnownUpperBound = objective.getUB();
        }
        this.strict = strict;
        this.alterator = new ObjectiveAlterer() {
            @Override
            public int offset(ReconfigurationProblem rp, int currentValue) {
                return 1;
            }
        };
    }

    /**
     * Creates an optimization manager
     * Enables to cut "worse" solutions
     *
     * @param objective variable (represent the value of a solution)
     * @param policy    SATISFACTION / MINIMIZATION / MAXIMIZATION
     * @param solver
     */
    public AlterableIntObjectiveManager(ReconfigurationProblem rp, final IntVar objective, ResolutionPolicy policy, Solver solver) {
        this(rp, objective, policy, solver, true);
    }

    public Integer getBestSolutionValue() {
        if (policy == ResolutionPolicy.MINIMIZE) {
            return bestKnownUpperBound;
        }
        if (policy == ResolutionPolicy.MAXIMIZE) {
            return bestKnownLowerBound;
        }
        throw new UnsupportedOperationException("There is no objective variable in satisfaction problems");
    }

    public IntVar getObjective() {
        return objective;
    }

    /**
     * @return the best lower bound computed so far
     */
    public int getBestLB() {
        return bestKnownLowerBound;
    }

    /**
     * States that lb is a global lower bound on the problem
     *
     * @param lb lower bound
     */
    public void updateBestLB(int lb) {
        bestKnownLowerBound = Math.max(bestKnownLowerBound, lb);
    }

    /**
     * States that ub is a global upper bound on the problem
     *
     * @param ub upper bound
     */
    public void updateBestUB(int ub) {
        bestKnownUpperBound = Math.min(bestKnownUpperBound, ub);
    }

    /**
     * @return the best upper bound computed so far
     */
    public int getBestUB() {
        return bestKnownUpperBound;
    }

    /**
     * Informs the manager that a new solution has been found
     */
    public void update() {
        if (policy == ResolutionPolicy.MINIMIZE) {
            this.bestKnownUpperBound = objective.getValue();
        } else if (policy == ResolutionPolicy.MAXIMIZE) {
            this.bestKnownLowerBound = objective.getValue();
        }
    }

    /**
     * Prevent the solver from computing worse quality solutions
     *
     * @throws solver.exception.ContradictionException
     */
    public void postDynamicCut() throws ContradictionException {
        int offset = 0;
        if (measures.getSolutionCount() > 0 && strict) {
            offset = 1;
        }
        if (policy == ResolutionPolicy.MINIMIZE) {
            this.objective.updateUpperBound(bestKnownUpperBound - alterator.offset(rp, bestKnownUpperBound), this);
            this.objective.updateLowerBound(bestKnownLowerBound, this);
        } else if (policy == ResolutionPolicy.MAXIMIZE) {
            this.objective.updateUpperBound(bestKnownUpperBound, this);
            this.objective.updateLowerBound(bestKnownLowerBound + alterator.offset(rp, bestKnownUpperBound), this);
        }
    }

    @Override
    public String toString() {
        switch (policy) {
            case MINIMIZE:
                return String.format("Minimize %s = %d", this.objective.getName(), bestKnownUpperBound);
            case MAXIMIZE:
                return String.format("Maximize %s = %d", this.objective.getName(), bestKnownLowerBound);
            case SATISFACTION:
                return "SAT";
            default:
                throw new UnsupportedOperationException("no objective manager");
        }
    }

    public void explain(Deduction val, Explanation e) {
        if (policy != ResolutionPolicy.SATISFACTION) {
            objective.explain(VariableState.DOM, e);
        }
    }

    public void setAlterer(ObjectiveAlterer a) {
        this.alterator = a;
    }
}
