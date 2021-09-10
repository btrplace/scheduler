/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
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
     * @param labels      the label describing each dimension
     * @param l           array of nbBins variables, each figuring the total size of the items assigned to it, usually initialized to [0, capacity]
     * @param s           array of nbItems variables, each figuring the item size. Only the LB will be considered!
     * @param b           array of nbItems variables, each figuring the possible bins an item can be assigned to, usually initialized to [0, nbBins-1]
     * @param cardinality {@code true} to indicate that the last dimension is the
     *                    number of items per bin.
     */
    public VectorPacking(String[] labels, IntVar[][] l, int[][] s, IntVar[] b
            , boolean cardinality) {
        super("VectorPacking",
                new VectorPackingPropagator(labels, l, s, b, cardinality));
    }

    /**
     * constructor of a 1D VectorPacking constraint.
     *
     * @param label the dimension name.
     * @param l     the load of every bin. Usually initialized to [0, capacity].
     * @param s     the size of every item.
     * @param b     the assignment to every item. Usually set to [0, nbBins-1].
     */
    public VectorPacking(String label, IntVar[] l, int[] s, IntVar[] b) {
        this(new String[]{label}, new IntVar[][]{l}, new int[][]{s}, b, false);
    }

    public IStateInt[][] assignedLoad() {
        return ((VectorPackingPropagator) propagators[0]).assignedLoad();
    }
}
