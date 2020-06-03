/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
