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

package btrplace.solver.choco.actionModel;

import btrplace.plan.ReconfigurationPlan;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Model an action.
 *
 * @author Fabien Hermenier
 * @see {@link ActionModelUtils} a utility class to extract components of ActionModels.
 */
public interface ActionModel {

    /**
     * Get the moment the action starts.
     *
     * @return a variable that must be positive
     */
    IntDomainVar getStart();

    /**
     * Get the moment the action ends.
     *
     * @return a variable that must be greater than {@link #getStart()}
     */
    IntDomainVar getEnd();

    /**
     * Get the action duration.
     *
     * @return a duration equals to {@code getEnd() - getStart()}
     */
    IntDomainVar getDuration();

    /**
     * Insert into a plan the actions resulting from the model.
     *
     * @param plan the plan to modify
     * @return {@code true} iff success
     */
    boolean insertActions(ReconfigurationPlan plan);

    /**
     * Get the next state of the subject manipulated by the action.
     *
     * @return {@code 0} for offline, {@code 1} for online.
     */
    IntDomainVar getState();

    /**
     * Make a visitor inspect the action model.
     *
     * @param v the visitor to use
     */
    void visit(ActionModelVisitor v);
}
