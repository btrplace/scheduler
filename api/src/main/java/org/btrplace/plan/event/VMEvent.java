/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

import org.btrplace.model.VM;

/**
 * A event to apply on a VM.
 *
 * @author Fabien Hermenier
 */
public interface VMEvent extends Event {

    /**
     * Get the VM to involved in the action.
     *
     * @return the VM identifier
     */
    VM getVM();
}
