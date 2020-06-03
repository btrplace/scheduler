/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint.mttr;

import org.btrplace.model.Node;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IntVar;

import java.util.List;


/**
 * A heuristic that first focus on the start moment for VM
 * actions that goes to a node without any outgoing actions.
 *
 * @author Fabien Hermenier
 */
public class StartOnLeafNodes implements VariableSelector<IntVar> {

  private final MovementGraph graph;

  private final List<Node> nodes;

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
