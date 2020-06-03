/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

import org.btrplace.model.Node;

/**
 * A event to apply on a node.
 *
 * @author Fabien Hermenier
 */
public interface NodeEvent extends Event {

    /**
     * Get the node to involved in the action.
     *
     * @return the node identifier
     */
    Node getNode();
}
