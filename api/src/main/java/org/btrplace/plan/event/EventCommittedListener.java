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
 * Interface to signal the termination of events.
 *
 * @author Fabien Hermenier
 */
public interface EventCommittedListener {

    /**
     * Notifies the termination of a {@link Allocate} action.
     *
     * @param a the terminated action
     */
    void committed(Allocate a);

    /**
     * Notifies the termination of a {@link AllocateEvent} event.
     *
     * @param a the event to visit
     */
    void committed(AllocateEvent a);

    /**
     * Notifies the termination of a {@link SubstitutedVMEvent} event.
     *
     * @param a the event to visit
     */
    void committed(SubstitutedVMEvent a);

    /**
     * Notifies the termination of a {@link BootNode} action.
     *
     * @param a the terminated action
     */
    void committed(BootNode a);

    /**
     * Notifies the termination of a {@link BootVM} action.
     *
     * @param a the terminated action
     */
    void committed(BootVM a);

    /**
     * Notifies the termination of a {@link ForgeVM} action.
     *
     * @param a the terminated action
     */
    void committed(ForgeVM a);

    /**
     * Notifies the termination of a {@link KillVM} action.
     *
     * @param a the terminated action
     */
    void committed(KillVM a);

    /**
     * Notifies the termination of a {@link MigrateVM} action.
     *
     * @param a the terminated action
     */
    void committed(MigrateVM a);

    /**
     * Notifies the termination of a {@link ResumeVM} action.
     *
     * @param a the terminated action
     */
    void committed(ResumeVM a);

    /**
     * Notifies the termination of a {@link ShutdownNode} action.
     *
     * @param a the terminated action
     */
    void committed(ShutdownNode a);

    /**
     * Notifies the termination of a {@link ShutdownVM} action.
     *
     * @param a the terminated action
     */
    void committed(ShutdownVM a);

    /**
     * Notifies the termination of a {@link SuspendVM} action.
     *
     * @param a the terminated action
     */
    void committed(SuspendVM a);
}
