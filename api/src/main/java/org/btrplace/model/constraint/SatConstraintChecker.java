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

package org.btrplace.model.constraint;

import org.btrplace.model.Model;
import org.btrplace.plan.event.*;

/**
 * Check if a constraint is satisfied by a reconfiguration plan.
 * <p>
 * The checking process is performed following an event-based approach
 * using an instance of {@link org.btrplace.plan.ReconfigurationPlanChecker}.
 * <p>
 * First, the checker is notified for the model at the origin of the
 * reconfiguration. It is then notified each time an action starts or ends
 * and finally, it is notified about the resulting model.
 * <p>
 * Actions notifications are propagated with regards to their starting
 * and ending moment. If an action ends at the same moment another action
 * starts, the notification for the ending action is send first.
 *
 * @author Fabien Hermenier
 * @see org.btrplace.plan.ReconfigurationPlanChecker
 */
public interface SatConstraintChecker<C extends SatConstraint> {

    /**
     * Notify for the model at the source of the reconfiguration.
     *
     * @param mo the model
     * @return {@code true} iff the model is valid wrt. the constraint
     */
    boolean startsWith(Model mo);

    /**
     * Notify for the beginning of an action.
     *
     * @param a the executed that will be executed
     * @return {@code true} iff the action execution is valid wrt. the constraint
     */
    boolean start(MigrateVM a);

    /**
     * Notify for the end of an action.
     *
     * @param a the action that ends
     */
    void end(MigrateVM a);

    /**
     * Notify for the beginning of an action.
     *
     * @param a the executed that will be executed
     * @return {@code true} iff the action execution is valid wrt. the constraint
     */
    boolean start(BootVM a);

    /**
     * Notify for the end of an action.
     *
     * @param a the action that ends
     */
    void end(BootVM a);

    /**
     * Notify for the beginning of an action.
     *
     * @param a the executed that will be executed
     * @return {@code true} iff the action execution is valid wrt. the constraint
     */
    boolean start(BootNode a);

    /**
     * Notify for the end of an action.
     *
     * @param a the action that ends
     */
    void end(BootNode a);

    /**
     * Notify for the beginning of an action.
     *
     * @param a the action that will be executed
     * @return {@code true} iff the action execution is valid wrt. the constraint
     */
    boolean start(ShutdownVM a);

    /**
     * Notify for the end of an action.
     *
     * @param a the action that ends
     */
    void end(ShutdownVM a);

    /**
     * Notify for the beginning of an action.
     *
     * @param a the action that will be executed
     * @return {@code true} iff the action execution is valid wrt. the constraint
     */
    boolean start(ShutdownNode a);

    /**
     * Notify for the end of an action.
     *
     * @param a the action that ends
     */
    void end(ShutdownNode a);

    /**
     * Notify for the beginning of an action.
     *
     * @param a the action that will be executed
     * @return {@code true} iff the action execution is valid wrt. the constraint
     */
    boolean start(ResumeVM a);

    /**
     * Notify for the end of an action.
     *
     * @param a the action that ends
     */
    void end(ResumeVM a);

    /**
     * Notify for the beginning of an action.
     *
     * @param a the action that will be executed
     * @return {@code true} iff the action execution is valid wrt. the constraint
     */
    boolean start(SuspendVM a);

    /**
     * Notify for the end of an action.
     *
     * @param a the action that ends
     */
    void end(SuspendVM a);

    /**
     * Notify for the beginning of an action.
     *
     * @param a the action that will be executed
     * @return {@code true} iff the action execution is valid wrt. the constraint
     */
    boolean start(KillVM a);

    /**
     * Notify for the end of an action.
     *
     * @param a the action that ends
     */
    void end(KillVM a);

    /**
     * Notify for the beginning of an action.
     *
     * @param a the action that will be executed
     * @return {@code true} iff the action execution is valid wrt. the constraint
     */
    boolean start(ForgeVM a);

    /**
     * Notify for the end of an action.
     *
     * @param a the action that ends
     */
    void end(ForgeVM a);

    /**
     * Notify for the beginning of an event.
     *
     * @param e the event that will be executed
     * @return {@code true} iff the action execution is valid wrt. the constraint
     */
    boolean consume(SubstitutedVMEvent e);

    /**
     * Notify for the beginning of an event.
     *
     * @param e the event that will be executed
     * @return {@code true} iff the action execution is valid wrt. the constraint
     */
    boolean consume(AllocateEvent e);

    /**
     * Notify for the beginning of an action.
     *
     * @param a the action that will be executed
     * @return {@code true} iff the action execution is valid wrt. the constraint
     */
    boolean start(Allocate a);

    /**
     * Notify for the end of an action.
     *
     * @param e the action that ends
     */
    void end(Allocate e);

    /**
     * Notify for the model that is reached once the reconfiguration has been applied.
     *
     * @param mo the model
     * @return {@code true} iff the model is valid wrt. the constraint
     */
    boolean endsWith(Model mo);

    /**
     * Get the constraint associated to the checker.
     *
     * @return a non-null constraint
     */
    C getConstraint();
}
