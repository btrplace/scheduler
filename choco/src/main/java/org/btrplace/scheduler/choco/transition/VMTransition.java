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

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.VM;
import org.btrplace.scheduler.choco.Slice;


/**
 * Interface to specify a transition over a VM.
 *
 * @author Fabien Hermenier
 */
public interface VMTransition extends Transition {

    /**
     * Get the VM manipulated by the action.
     *
     * @return the VM identifier
     */
    VM getVM();

    /**
     * Get the slice denoting the possible current placement of the subject on a node.
     *
     * @return a {@link org.btrplace.scheduler.choco.Slice} that may be {@code null}
     */
    Slice getCSlice();

    /**
     * Get the slice denoting the possible future placement off the subject
     *
     * @return a {@link Slice} that may be {@code null}
     */
    Slice getDSlice();

    /**
     * Tell if this actions allow the management of the VM.
     * Typically if it is possible to have a resulting action.
     *
     * @return {@code true} if the VM must be manipulated
     */
    boolean isManaged();
}
