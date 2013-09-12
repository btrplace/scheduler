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

package btrplace.solver.choco;

import choco.kernel.solver.variables.Var;

/**
 * A class to provide a method to customize the optimisation process of the solver.
 * Instead of trying all the possible values, you will be able to choose the value to try
 * for the objective variable. This may speed up the improvement phase.
 * <p/>
 * As the solver will not longer try all the possible values, the completeness of
 * the resolution process is no longer guarantee and may remove some (possibly good) solutions.
 *
 * @author Fabien Hermenier
 */
public abstract class ObjectiveAlterer {

    /**
     * The objective variable.
     */
    private Var obj;

    /**
     * The reconfiguration problem to consider.
     */
    private ReconfigurationProblem rp;

    /**
     * Make a new alterer on a given problem.
     * The objective variable must have been declared.
     *
     * @param p the reconfiguration problem to consider
     */
    public ObjectiveAlterer(ReconfigurationProblem p) {
        this.rp = p;
        obj = p.getSolver().getObjective();
    }

    /**
     * Compute a new target bound for the objective one a solution has been computed.
     *
     * @param currentValue the current value of the objective
     * @return the new bound to set. It must stay within the objective variable bounds to continue the solving process
     */
    public abstract int tryNewValue(int currentValue);

    @Override
    public String toString() {
        return new StringBuilder("objectiveAlterer(").append(obj.getName()).append(')').toString();
    }

    /**
     * Get the objective.
     *
     * @return the objective variable.
     */
    public Var getObjective() {
        return obj;
    }

    /**
     * Get the reconfiguration problem associated to that objective.
     *
     * @return the reconfiguration problem
     */
    public ReconfigurationProblem getReconfigurationProblem() {
        return rp;
    }
}
