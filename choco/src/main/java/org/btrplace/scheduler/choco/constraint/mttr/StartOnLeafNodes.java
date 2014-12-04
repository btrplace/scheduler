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

package org.btrplace.scheduler.choco.constraint.mttr;

import org.btrplace.model.Node;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.solver.search.strategy.selectors.VariableSelector;
import org.chocosolver.solver.variables.IntVar;

import java.util.List;


/**
 * A heuristic that first focus on the start moment for VM
 * actions that goes to a node without any outgoing actions.
 *
 * @author Fabien Hermenier
 */
public class StartOnLeafNodes implements VariableSelector<IntVar> {

    private MovementGraph graph;

    private Node[] nodes;

    /**
     * Make a new heuristics
     *
     * @param rp the problem
     * @param g  the movement graph
     */
    public StartOnLeafNodes(ReconfigurationProblem rp, MovementGraph g) {
        this.graph = g;
        nodes = rp.getNodes();
    }

    @Override
    public IntVar getVariable(IntVar[] scope) {
        // todo check coherence between scope (Dslices) and graph vars (Cslices)
        graph.make();
        for (Node n : nodes) {
            List<IntVar> outs = graph.getOutgoing(n);
            if (outs.isEmpty()) {
                for (IntVar v : graph.getIncoming(n)) {
                    if (!v.isInstantiated()) {
                        return v;
                    }
                }
            }
        }
        return null;
    }
}
