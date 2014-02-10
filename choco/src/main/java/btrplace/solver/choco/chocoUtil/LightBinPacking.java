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

package btrplace.solver.choco.chocoUtil;


import memory.IEnvironment;
import memory.IStateBitSet;
import memory.IStateBool;
import memory.IStateInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.constraints.IntConstraint;
import solver.variables.IntVar;
import util.ESat;
import util.tools.ArrayUtils;

/**
 * Lighter but faster version of BinPacking that does not provide the knapsack filtering
 *
 * @author Fabien Hermenier
 */
public class LightBinPacking extends IntConstraint<IntVar> {

    private static final Logger LOGGER = LoggerFactory.getLogger("solver");
    /**
     * The solver environment.
     */
    private IEnvironment env;

    /**
     * The number of bins.
     */
    private final int nbBins;

    private final int nbDims;

    /**
     * The bin assigned to each item.
     */
    private final IntVar[] bins;

    /**
     * The constant size of each item in decreasing order.
     * [nbDims][nbItems]
     */
    private final int[][] iSizes;

    /**
     * The sum of the item sizes per dimension. [nbItems]
     */
    private long[] sumISizes;

    /**
     * The load of each bin per dimension. [nbDims][nbBins]
     */
    private final IntVar[][] loads;

    /**
     * The total size of the required + candidate items for each bin. [nbDims][nbBins]
     */
    private IStateInt[][] bTLoads;

    /**
     * The total size of the required items for each bin. [nbDims][nbBins]
     */
    private IStateInt[][] bRLoads;

    /**
     * The sum of the bin load LBs. [nbDims]
     */
    private IStateInt[] sumLoadInf;

    /**
     * The sum of the bin load UBs. [nbDims]
     */
    private IStateInt[] sumLoadSup;

    /**
     * Has some bin load variable changed since the last propagation ?
     */
    private IStateBool loadsHaveChanged;

    private String[] name;

    private IStateBitSet notEntailedDims;

    /**
     * constructor of the FastBinPacking global constraint
     *
     * @param labels      the label describing each dimension
     * @param environment the solver environment
     * @param l           array of nbBins variables, each figuring the total size of the items assigned to it, usually initialized to [0, capacity]
     * @param s           array of nbItems variables, each figuring the item size. Only the LB will be considered!
     * @param b           array of nbItems variables, each figuring the possible bins an item can be assigned to, usually initialized to [0, nbBins-1]
     */
    public LightBinPacking(String[] labels, IEnvironment environment, IntVar[][] l, int[][] s, IntVar[] b) {
        super(ArrayUtils.append(b, ArrayUtils.flatten(l)), l[0][0].getSolver());
        this.name = labels;
        this.env = environment;
        this.loads = l;
        this.nbBins = l[0].length;
        this.nbDims = l.length;
        this.bins = b;
        this.iSizes = s;
        this.bTLoads = new IStateInt[nbDims][nbBins];
        this.bRLoads = new IStateInt[nbDims][nbBins];
        setPropagators(new LightBinPackingPropagator(labels, env, l, s, b));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
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
            for (int d = notEntailedDims.nextSetBit(0); d >= 0; d = notEntailedDims.nextSetBit(d + 1)) {
                int loadPos = iSizes[0].length + d * nbBins + b;
                if (tuple[loadPos] != l[d][b]) {
                    LOGGER.warn("Invalid load for bin " + b + " on dimension " + d + ". Was " + tuple[loadPos] + ", expected " + l[d][b]);
                    return ESat.FALSE;
                }
            }
        }
        return ESat.TRUE;
    }

}