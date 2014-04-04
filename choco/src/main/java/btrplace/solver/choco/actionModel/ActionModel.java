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

package btrplace.solver.choco.actionModel;

import btrplace.plan.ReconfigurationPlan;
import solver.variables.BoolVar;
import solver.variables.IntVar;


/**
 * Model an action.
 * See {@link ActionModelUtils} to extract components of a collection of ActionModel.
 *
 * @author Fabien Hermenier
 * @see ActionModelUtils
 */
public interface ActionModel {

    /**
     * Get the moment the action starts.
     *
     * @return a variable that must be positive
     */
    IntVar getStart();

    /**
     * Get the moment the action ends.
     *
     * @return a variable that must be greater than {@link #getStart()}
     */
    IntVar getEnd();

    /**
     * Get the action duration.
     *
     * @return a duration equals to {@code getEnd() - getStart()}
     */
    IntVar getDuration();

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
    BoolVar getState();
}
