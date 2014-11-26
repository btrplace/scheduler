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
 * This allows to monitor the execution of a reconfiguration plan while
 * considering the dependencies between the actions that are established
 * in a {@link ReconfigurationPlan}.
 * <p>
 * With regards to the actions that have already been executed, it
 * is possible to get the actions that can be safely executed.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlanMonitor {

    /**
     * Get the current model.
     *
     * @return a model
     */
    Model getCurrentModel();

    /**
     * Commit an action that was applyable.
     * If it is theoretically possible to execute the action on the current model,
     * the model is updated accordingly.
     *
     * @param a the action to commit
     * @return a set of unblocked actions that may be empty if the operation succeed.
     * {@code null} if the commit was not allowed because the action was not applyable
     */
    Set<Action> commit(Action a);

    /**
     * Get the number of actions that have been committed.
     *
     * @return a number between 0 and {@link org.btrplace.plan.ReconfigurationPlan#getSize()}
     */
    int getNbCommitted();

    /**
     * Check if an action is blocked.
     *
     * @param a the action to check
     * @return {@code true} iff the action is blocked
     */
    boolean isBlocked(Action a);

    /**
     * Get the plan associated to the monitor.
     *
     * @return a non-null plan
     */
    ReconfigurationPlan getReconfigurationPlan();
}
