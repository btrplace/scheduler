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

package org.btrplace.plan;

import org.btrplace.model.Model;
import org.btrplace.plan.event.Action;

import java.util.Set;

/**
 * A reconfiguration plan is a set of actions to execute
 * to reconfigure an infrastructure starting from a given model.
 * <p>
 * Actions must be sorted using a {@link TimedBasedActionComparator} that differentiate
 * simultaneous but not equals actions.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlan extends Iterable<Action> {

    /**
     * Get the model that is used as a starting point
     * to perform the reconfiguration.
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
     * Get the number of actions in the plan.
     *
     * @return a positive integer
     */
    int getSize();

    /**
     * Return the theoretical duration of a reconfiguration plan.
     *
     * @return the end moment of the last executed action
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
     * @return the resulting model or {@code null} if the plan cannot be applied
     */
    Model getResult();

    /**
     * Check if all the actions can be applied according
     * to their schedule.
     * In practice, it calls every {@link Action#apply(org.btrplace.model.Model)} while
     * respecting their ordering
     *
     * @return {@code true} iff the actions can be applied
     */
    boolean isApplyable();

    /**
     * Get the actions that have to be executed before
     * a given action. Transitive dependencies are ignored.
     *
     * @param a the action
     * @return a set of dependencies that may be empty.
     */
    Set<Action> getDirectDependencies(Action a);

    /**
     * Get the applier that is used to simulate the actions application
     * on the starting model.
     *
     * @return the applier in use.
     */
    ReconfigurationPlanApplier getReconfigurationApplier();

    /**
     * Set the applier to use to simulate the actions application.
     *
     * @param ra the applier to use
     */
    void setReconfigurationApplier(ReconfigurationPlanApplier ra);
}
