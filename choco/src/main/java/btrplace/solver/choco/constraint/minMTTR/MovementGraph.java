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

package btrplace.solver.choco.constraint.minMTTR;

import btrplace.model.Node;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.actionModel.VMActionModel;
import solver.variables.IntVar;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class MovementGraph {

    private ReconfigurationProblem rp;

    private Map<Node, List<IntVar>> incomings;

    private Map<Node, List<IntVar>> outgoings;

    public MovementGraph(ReconfigurationProblem rp) {
        this.rp = rp;
        incomings = new HashMap<>();
        outgoings = new HashMap<>();
    }

    public void make() {
        incomings.clear();
        outgoings.clear();

        for (VMActionModel a : rp.getVMActions()) {
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
        List<IntVar> l = outgoings.get(h);
        if (l == null) {
            l = new ArrayList<>();
            outgoings.put(h, l);
        }
        l.add(cSlice.getStart());
    }

    private void addIncoming(Slice cSlice) {
        Node h = rp.getNode(cSlice.getHoster().getLB());
        List<IntVar> l = incomings.get(h);
        if (l == null) {
            l = new ArrayList<>();
            incomings.put(h, l);
        }
        l.add(cSlice.getStart());
    }

    public List<IntVar> getIncoming(Node n) {
        List<IntVar> l = incomings.get(n);
        if (l == null) {
            l = Collections.emptyList();
        }
        return l;
    }

    public List<IntVar> getOutgoing(Node n) {
        List<IntVar> l = outgoings.get(n);
        if (l == null) {
            l = Collections.emptyList();
        }
        return l;

    }
}
