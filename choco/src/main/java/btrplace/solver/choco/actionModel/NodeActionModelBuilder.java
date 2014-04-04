/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.actionModel;

import btrplace.model.Node;
import btrplace.model.NodeState;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;

/**
 * A builder to instantiate a {@link btrplace.solver.choco.actionModel.NodeActionModelBuilder}
 *
 * @author Fabien Hermenier
 */
public abstract class NodeActionModelBuilder {

    /**
     * The initial state of the node.
     */
    private NodeState s;

    private String id;

    /**
     * New builder.
     *
     * @param src the initial state of the node.
     */
    public NodeActionModelBuilder(String id, NodeState src) {
        this.id = id;
        this.s = src;
    }

    /**
     * Build the {@link btrplace.solver.choco.actionModel.NodeActionModel}
     *
     * @param rp the current problem
     * @param n  the manipulated node
     * @return the resulting model
     * @throws btrplace.solver.SolverException if an error occurred while building the model
     */
    public abstract NodeActionModel build(ReconfigurationProblem rp, Node n) throws SolverException;

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
