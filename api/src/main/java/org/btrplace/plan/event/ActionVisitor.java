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
