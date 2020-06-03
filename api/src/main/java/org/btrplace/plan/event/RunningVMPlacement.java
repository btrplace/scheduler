/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

import org.btrplace.model.Node;

/**
 * An interface to indicate an event that
 * will place a running VM on a node.
 *
 * @author Fabien Hermenier
 */
public interface RunningVMPlacement extends VMEvent {

    /**
     * Get the destination node for the VM.
     *
     * @return the node identifier
     */
    Node getDestinationNode();
}
