/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.Node;
import org.btrplace.model.NodeState;
import org.chocosolver.solver.variables.IntVar;

/**
 * Interface to specify a transition that manipulates a node.
 *
 * @author Fabien Hermenier
 */
public interface NodeTransition extends Transition<NodeState> {

    /**
     * Get the node manipulated by the action.
     *
     * @return the node identifier
     */
    Node getNode();

    /**
     * Get the moment the node is being capable of hosting VMs.
     *
     * @return a variable
     */
    IntVar getHostingStart();

    /**
     * Get the moment the node is no longer capable of hosting VMs.
     *
     * @return a variable
     */
    IntVar getHostingEnd();

    /**
     * Get the node initial state.
     * @return a state
     */
    NodeState getSourceState();
}
