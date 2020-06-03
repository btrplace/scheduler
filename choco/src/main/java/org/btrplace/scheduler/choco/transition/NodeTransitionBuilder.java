/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.Node;
import org.btrplace.model.NodeState;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;

/**
 * A builder to instantiate a {@link NodeTransitionBuilder}
 *
 * @author Fabien Hermenier
 */
public abstract class NodeTransitionBuilder {

  /**
   * The initial state of the node.
   */
  private final NodeState s;

    private final String id;

  /**
     * New builder.
     *
     * @param lbl the transition label
     * @param src the initial state of the node.
     */
    public NodeTransitionBuilder(String lbl, NodeState src) {
        this.id = lbl;
        this.s = src;
    }

    /**
     * Build the {@link NodeTransition}
     *
     * @param rp the current problem
     * @param n  the manipulated node
     * @return the resulting model
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred while building the model
     */
    public abstract NodeTransition build(ReconfigurationProblem rp, Node n) throws SchedulerException;

    /**
     * Get the initial state of the node.
     *
     * @return a state
     */
    public NodeState getSourceState() {
        return s;
    }

    @Override
    public String toString() {
        return s + " : " + id;
    }
}
