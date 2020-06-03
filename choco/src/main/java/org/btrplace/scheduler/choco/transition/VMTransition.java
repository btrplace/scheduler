/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.VM;
import org.btrplace.model.VMState;
import org.btrplace.scheduler.choco.Slice;


/**
 * Interface to specify a transition over a VM.
 *
 * @author Fabien Hermenier
 */
public interface VMTransition extends Transition<VMState> {

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

    /**
     * Get the VM initial state.
     * @return a state
     */
    VMState getSourceState();

    /**
     * Get the future VM state.
     * @return a state
     */
    VMState getFutureState();
}
