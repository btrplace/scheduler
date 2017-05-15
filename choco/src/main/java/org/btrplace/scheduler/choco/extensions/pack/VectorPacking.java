/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco.extensions.pack;


import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

/**
 * Lighter but faster version of BinPacking that does not provide the knapsack filtering
 *
 * @author Fabien Hermenier
 */
public class VectorPacking extends Constraint {


    /**
     * constructor of the FastBinPacking global constraint
     *
     * @param labels   the label describing each dimension
     * @param l        array of nbBins variables, each figuring the total size of the items assigned to it, usually initialized to [0, capacity]
     * @param s        array of nbItems variables, each figuring the item size. Only the LB will be considered!
     * @param b        array of nbItems variables, each figuring the possible bins an item can be assigned to, usually initialized to [0, nbBins-1]
     */
    public VectorPacking(String[] labels, IntVar[][] l, int[][] s, IntVar[] b) {
        super("VectorPacking", new VectorPackingPropagator(labels, l, s, b));
    }

    public IStateInt[][] assignedLoad() {
        return ((VectorPackingPropagator) propagators[0]).assignedLoad();
    }
}
