/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
     * @throws InfeasibleActionException if the action was not applyable.
     */
    Set<Action> commit(Action a) throws InfeasibleActionException;

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
