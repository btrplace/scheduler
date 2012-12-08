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

package btrplace.plan;

import btrplace.model.Model;

import java.util.Set;

/**
 * A reconfiguration plan is a set of actions to execute
 * to reconfigure an infrastructure starting from a given model.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlan extends Iterable<Action> {

    /**
     * Get the model that is used as a starting point
     * to perform the reconfiguration
     *
     * @return the original model
     */
    Model getOrigin();

    /**
     * Add a new action to a plan.
     *
     * @param a the action to add
     * @return {@code true} iff the action has been added
     */
    boolean add(Action a);

    /**
     * Get the number of action in the plan.
     *
     * @return a positive integer
     */
    int getSize();

    /**
     * Return the theoretical duration of a reconfiguration plan.
     *
     * @return the finish moment of the last action to execute
     */
    int getDuration();

    /**
     * Get all the actions to perform.
     *
     * @return a list of actions. May be empty
     */
    Set<Action> getActions();

    /**
     * Get the resulting model once all the actions are executed.
     *
     * @return the resulting model or {@code null} if the plan is not applyable.
     */
    Model getResult();

    /**
     * Check if all the actions can be applied according
     * to their schedule.
     * In practice, it calls every {@link Action#apply(btrplace.model.Model)} while
     * respecting their ordering
     *
     * @return {@code true} iff the actions are applyable.
     */
    boolean isApplyable();
}
