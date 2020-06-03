/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

/**
 * A visitor compatible will all actions supported by btrplace.
 *
 * @author Fabien Hermenier
 */
public interface ActionVisitor {

    /**
     * Visit a {@link Allocate} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(Allocate a);

    /**
     * Visit a {@link AllocateEvent} event.
     *
     * @param a the event to visit
     * @return a possible value
     */
    Object visit(AllocateEvent a);

    /**
     * Visit a {@link SubstitutedVMEvent} event.
     *
     * @param a the event to visit
     * @return a possible value
     */
    Object visit(SubstitutedVMEvent a);

    /**
     * Visit a {@link BootNode} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(BootNode a);

    /**
     * Visit a {@link BootVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(BootVM a);

    /**
     * Visit a {@link ForgeVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(ForgeVM a);

    /**
     * Visit a {@link KillVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(KillVM a);

    /**
     * Visit a {@link MigrateVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(MigrateVM a);

    /**
     * Visit a {@link ResumeVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(ResumeVM a);

    /**
     * Visit a {@link ShutdownNode} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(ShutdownNode a);

    /**
     * Visit a {@link ShutdownVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(ShutdownVM a);

    /**
     * Visit a {@link SuspendVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(SuspendVM a);
}
