/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
import org.chocosolver.solver.variables.IntVar;

import java.util.Comparator;

/**
 * Created by fhermeni on 18/02/2015.
 */
public class CapacityComparator implements Comparator<Node> {

    private ReconfigurationProblem rp;

    private int order;
    public CapacityComparator(ReconfigurationProblem rp, boolean asc) {
            this.rp = rp;
            order = asc ? 1 : -1;
        }

    @Override
    public int compare(Node n1, Node n2) {
        IntVar c1 = rp.getNbRunningVMs().get(rp.getNode(n1));
        IntVar c2 = rp.getNbRunningVMs().get(rp.getNode(n2));
        return order * (c1.getLB() - c2.getLB());
    }
}
