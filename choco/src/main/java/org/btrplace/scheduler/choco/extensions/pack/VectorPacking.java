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

package org.btrplace.scheduler.choco.extensions.pack;


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
     * @param withHeap optional: process bins in a heap if true
     * @param withKS   optional: process knapsack filtering on each bin bif true
     */
    public VectorPacking(String[] labels, IntVar[][] l, int[][] s, IntVar[] b, boolean withHeap, boolean withKS) {
        super("VectorPacking", new VectorPackingPropagator(labels, l, s, b, withHeap, withKS));
    }


/*    public ESat isSatisfied(int[] tuple) {
        int[][] l = new int[nbDims][nbBins];
        int[][] c = new int[nbDims][nbBins];
        for (int i = 0; i < bins.length; i++) {
            final int b = tuple[i];
            for (int d = 0; d < nbDims; d++) {
                l[d][b] += iSizes[d][i];
                c[d][b]++;
            }
        }
        for (int b = 0; b < nbBins; b++) {
            for (int d = 0; d < nbDims; d++) {
                int loadPos = iSizes[0].length + d * nbBins + b;
                if (tuple[loadPos] != l[d][b]) {
                    LOGGER.warn("Invalid load for bin " + b + " on dimension " + d + ". Was " + tuple[loadPos] + ", expected " + l[d][b]);
                    return ESat.FALSE;
                }
            }
        }
        return ESat.TRUE;
    }
    */

}
