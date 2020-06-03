/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

import org.btrplace.model.VMState;

/**
 * A interface to indicate an event realize a transition
 * on a VM state.
 *
 * @author Fabien Hermenier
 */
public interface VMStateTransition extends VMEvent {

    /**
     * Get the current state of the VM.
     *
     * @return a State
     */
    VMState getCurrentState();

    /**
     * Get the next state of the VM.
     *
     * @return a State
     */
    VMState getNextState();
}
