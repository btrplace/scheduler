/*
 * Copyright (c) 2019 University Nice Sophia Antipolis
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

    private ReconfigurationProblem rp;

    private Map<Node, List<IntVar>> incoming;

    private Map<Node, List<IntVar>> outgoings;

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
