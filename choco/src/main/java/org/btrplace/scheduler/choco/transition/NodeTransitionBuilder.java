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
    private NodeState s;

    private String id;

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
