/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint.mttr;

import org.btrplace.model.Node;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class denoting the movement of the running VMs from their source to their destination node.
 *
 * @author Fabien Hermenier
 */
public class MovementGraph {

  private final ReconfigurationProblem rp;

  private final Map<Node, List<IntVar>> incoming;

  private final Map<Node, List<IntVar>> outgoings;

    /**
     * Make a new graph.
     *
     * @param p the associated problem
     */
    public MovementGraph(ReconfigurationProblem p) {
        this.rp = p;
        incoming = new HashMap<>();
        outgoings = new HashMap<>();
    }

    public void make() {
        incoming.clear();
        outgoings.clear();

        for (VMTransition a : rp.getVMActions()) {
            Slice cSlice = a.getCSlice();
            Slice dSlice = a.getDSlice();

            if (cSlice != null) {
                addOutgoing(cSlice);
            }
            if (dSlice != null) {
                addIncoming(dSlice);
            }
        }
    }

    private void addOutgoing(Slice cSlice) {
        Node h = rp.getNode(cSlice.getHoster().getLB());
        outgoings.putIfAbsent(h, new ArrayList<>());
        outgoings.get(h).add(cSlice.getStart());
    }

    private void addIncoming(Slice cSlice) {
        Node h = rp.getNode(cSlice.getHoster().getLB());
        incoming.putIfAbsent(h, new ArrayList<>());
        incoming.get(h).add(cSlice.getStart());
    }

    /**
     * Get the start moment of the movements that terminate
     * on a given node
     *
     * @param n the destination node
     * @return a list of start moment. May be empty
     */
    public List<IntVar> getIncoming(Node n) {
        return incoming.getOrDefault(n, Collections.emptyList());
    }

    /**
     * Get the start moment of the movements that leave
     * from a given node
     *
     * @param n the source node
     * @return a list of start moment. May be empty
     */
    public List<IntVar> getOutgoing(Node n) {
        return outgoings.getOrDefault(n, Collections.emptyList());
    }
}
