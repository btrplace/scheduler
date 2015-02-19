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
import org.chocosolver.solver.variables.IntVar;

/**
 * Interface to specify a transition that manipulates a node.
 *
 * @author Fabien Hermenier
 */
public interface NodeTransition extends Transition {

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
