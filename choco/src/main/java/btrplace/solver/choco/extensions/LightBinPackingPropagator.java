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

package btrplace.solver.choco.extensions;


import memory.IEnvironment;
import memory.IStateBitSet;
import memory.IStateBool;
import memory.IStateInt;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.iterators.DisposableIntIterator;
import util.iterators.DisposableValueIterator;
import util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * Lighter but faster version of a bin packing that does not provide the knapsack filtering
 *
 * @author Fabien Hermenier
 */
public class LightBinPackingPropagator extends Propagator<IntVar> {

    private boolean first = true;
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
    public LightBinPackingPropagator(String[] labels, IEnvironment environment, IntVar[][] l, int[][] s, IntVar[] b) {
        super(ArrayUtils.append(b, ArrayUtils.flatten(l)), PropagatorPriority.VERY_SLOW, true);
        this.name = labels;
        this.env = environment;
        this.loads = l;
        this.nbBins = l[0].length;
        this.nbDims = l.length;
        this.bins = b;
        this.iSizes = s;
        this.bTLoads = new IStateInt[nbDims][nbBins];
        this.bRLoads = new IStateInt[nbDims][nbBins];
    }

    public boolean isConsistent() {
        int[][] l = new int[nbDims][nbBins];
        for (int i = 0; i < bins.length; i++) {
            if (bins[i].instantiated()) {
                for (int d = 0; d < nbDims; d++) {
                    int v = bins[i].getValue();
                    l[d][v] += iSizes[d][i];
                    if (l[d][v] > loads[d][v].getUB()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //****************************************************************//
    //********* Events ***********************************************//
    //****************************************************************//

    @Override
    public int getPropagationConditions(int idx) {
        if (idx < bins.length) {
            return EventType.REMOVE.mask;
        }
        return EventType.BOUND.mask;
    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }


    /**
     * initialize the internal data: availableBins, candidates, binRequiredLoads, binTotalLoads, sumLoadInf, sumLoadSup
     * shrink the item-to-bins assignment variables: 0 <= bins[i] <= nbBins
     * shrink the bin load variables: binRequiredLoad <= binLoad <= binTotalLoad
     */
    public void awake() throws ContradictionException {

        if (!first) {
            return;
        }
        first = false;
        sumISizes = new long[nbDims];
        notEntailedDims = env.makeBitSet(nbDims);
        notEntailedDims.clear(0, 3);

        computeSums();

        int[][] rLoads = new int[nbDims][nbBins];
        int[][] cLoads = new int[nbDims][nbBins];

        int[] nbUnassigned = new int[nbDims];
        int[] cs = new int[nbBins];
        for (int i = 0; i < bins.length; i++) {
            bins[i].updateLowerBound(0, aCause);
            bins[i].updateUpperBound(nbBins - 1, aCause);
            if (bins[i].instantiated()) {
                for (int d = 0; d < nbDims; d++) {
                    rLoads[d][bins[i].getValue()] += iSizes[d][i];
                }
            } else {
                for (int d = 0; d < nbDims; d++) {
                    nbUnassigned[d] += iSizes[d][i];
                }
                DisposableValueIterator it = bins[i].getValueIterator(true);
                try {
                    while (it.hasNext()) {
                        int b = it.next();
                        for (int d = 0; d < nbDims; d++) {
                            cLoads[d][b] += iSizes[d][i];
                            cs[b]++;
                        }
                    }
                } finally {
                    it.dispose();
                }
            }
        }

        int[] slb = new int[nbDims];
        int[] slu = new int[nbDims];
        for (int b = 0; b < nbBins; b++) {
            for (int d = 0; d < nbDims; d++) {
                bRLoads[d][b] = env.makeInt(rLoads[d][b]);
                bTLoads[d][b] = env.makeInt(rLoads[d][b] + cLoads[d][b]);
                loads[d][b].updateLowerBound(rLoads[d][b], aCause);
                loads[d][b].updateUpperBound(rLoads[d][b] + cLoads[d][b], aCause);
                slb[d] += loads[d][b].getLB();
                slu[d] += loads[d][b].getUB();
            }
        }

        sumLoadInf = new IStateInt[nbDims];
        sumLoadSup = new IStateInt[nbDims];
        for (int d = 0; d < nbDims; d++) {
            this.sumLoadInf[d] = env.makeInt(slb[d]);
            this.sumLoadSup[d] = env.makeInt(slu[d]);
        }

        this.loadsHaveChanged = env.makeBool(false);

        detectEntailedDimensions(nbUnassigned);

        assert checkLoadConsistency();
        LOGGER.trace("BinPacking: " + Arrays.toString(name) + " notEntailed dimensions: " + notEntailedDims);
        forcePropagate(EventType.INSTANTIATE);
    }

    /**
     * Detect entailed dimensions
     * A dimension is entailed if for every bin, the free load (diff between the UB and the LB) is
     * >= the un-assigned height for that dimension
     *
     * @param nbUnassigned the un-assigned height for each dimension.
     */
    private void detectEntailedDimensions(int[] nbUnassigned) {
        for (int d = 0; d < nbDims; d++) {
            for (int b = 0; b < nbBins; b++) {
                if (!loads[d][b].instantiated() && loads[d][b].getUB() - loads[d][b].getLB() < nbUnassigned[d]) {
                    notEntailedDims.set(d);
                    break;
                }
            }
        }

    }

    /**
     * Compute the sum of the demand for each dimension.
     */
    private void computeSums() {
        for (int d = 0; d < nbDims; d++) {
            long sum = 0;
            for (int i = 0; i < iSizes[d].length; i++) {
                sum += iSizes[d][i];
            }
            this.sumISizes[d] = sum;
        }

    }

    @Override
    /**
     * propagate 1) globally: sumItemSizes == sumBinLoads 2) on each bin: sumAssignedItemSizes == binLoad
     * rule 1.0: if sumSizes < sumBinLoadInfs or sumSizes > sumBinLoadSups then fail
     * rule 1.1, for each bin: sumItemSizes - sumOtherBinLoadSups <= binLoad <= sumItemSizes - sumOtherBinLoadInfs
     * rule 2.0, for each bin: binRequiredLoad <= binLoad <= binTotalLoad
     * rule 2.1, for each bin and candidate item: if binRequiredLoad + itemSize > binLoadSup then remove item from bin
     * rule 2.2, for each bin and candidate item: if binTotalLoad - itemSize < binLoadInf then pack item into bin
     * with "big items" optimization, the last rule is not valid for big items but can be replaced by:
     * rule 2.3: if smallItemSizes < binLoadInf then remove big candidates with size < binLoadInf-smallItemSizes
     * and update binLoadInf as binRequiredLoad + the size of the smallest big remaining candidate
     */
    public void propagate(int mask) throws ContradictionException {
        awake();
        recomputeLoadSums();
        for (int d = 0; d < nbDims; d++) {
            if (sumISizes[d] > sumLoadSup[d].get() || sumISizes[d] < sumLoadInf[d].get()) {
                contradiction(null, "");
            }
        }
        assert checkLoadConsistency();
    }

    @Override
    public void propagate(int idx, int mask) throws ContradictionException {
        if (EventType.isBound(mask)) {
            awakeOnRemovals(idx, null);
        }
        if (EventType.isInclow(mask)) {
            awakeOnInf(idx);
        }
        if (EventType.isDecupp(mask)) {
            awakeOnSup(idx);
        }

    }

    /**
     * recompute the sum of the min/max loads only if at least one variable bound has been updated outside the constraint
     */
    private boolean recomputeLoadSums() {
        if (!loadsHaveChanged.get()) {
            return false;
        }
        loadsHaveChanged.set(false);
        for (int d = 0; d < nbDims; d++) {
            int sli = 0;
            int sls = 0;
            for (int b = 0; b < nbBins; b++) {
                sli += loads[d][b].getLB();
                sls += loads[d][b].getUB();
            }

            this.sumLoadInf[d].set(sli);
            this.sumLoadSup[d].set(sls);
        }
        return true;
    }

    /**
     * on loads variables: delay propagation
     */
    //@Override
    public void awakeOnInf(int varIdx) throws ContradictionException {
        loadsHaveChanged.set(true);
        forcePropagate(EventType.INSTANTIATE);
        //constAwake(false);
    }

    /**
     * on loads variables: delay propagation
     */
    //@Override
    public void awakeOnSup(int varIdx) throws ContradictionException {
        loadsHaveChanged.set(true);
        forcePropagate(EventType.INSTANTIATE);
        //constAwake(false);
    }


    /**
     * on bins variables: propagate the removal of item-to-bins assignments.
     * 1) update the candidate and check to decrease the load UB of each removed bins: binLoad <= binTotalLoad
     * 2) if item is assigned: update the required and check to increase the load LB of the bin: binLoad >= binRequiredLoad
     *
     * @throws solver.exception.ContradictionException on the load variables
     */
    //@Override
    public void awakeOnRemovals(int iIdx, DisposableIntIterator deltaDomain) throws ContradictionException {
        try {
            while (deltaDomain.hasNext()) {
                int b = deltaDomain.next();
                removeItem(iIdx, b);
            }

        } finally {
            deltaDomain.dispose();
        }
        if (vars[iIdx].getLB() == vars[iIdx].getUB()) {
            assignItem(iIdx, vars[iIdx].getValue());
        }
        forcePropagate(EventType.INSTANTIATE);
        //this.constAwake(false);
    }

    //****************************************************************//
    //********* VARIABLE FILTERING ***********************************//
    //****************************************************************//

    /**
     * synchronize the internal data when an item is assigned to a bin:
     * remove the item from the candidate list of the bin and balance its size from the candidate to the required load of the bin
     * check to update the LB of load[bin]
     *
     * @param item item index in the bin
     * @param bin  bin index
     * @throws solver.exception.ContradictionException on the load[bin] variable
     */
    private void assignItem(int item, int bin) throws ContradictionException {
        for (int d = 0; d < nbDims; d++) {
            int r = bRLoads[d][bin].add(iSizes[d][item]);
            filterLoadInf(d, bin, r);
        }
    }

    /**
     * synchronize the internal data when an item is removed from a bin:
     * remove the item from the candidate list of the bin and reduce the candidate load of the bin
     * check to update the UB of load[bin]
     *
     * @param item item index in its bin
     * @param bin  bin index
     * @throws solver.exception.ContradictionException on the load[bin] variable
     */
    private void removeItem(int item, int bin) throws ContradictionException {
        for (int d = notEntailedDims.nextSetBit(0); d >= 0; d = notEntailedDims.nextSetBit(d + 1)) {
            int r = bTLoads[d][bin].add(-1 * iSizes[d][item]);
            filterLoadSup(d, bin, r);
        }
    }

    /**
     * increase the LB of the bin load and the sum of the bin load LBs
     *
     * @param bin        bin index
     * @param newLoadInf new LB of the bin load
     * @return {@code true} if LB is increased.
     * @throws solver.exception.ContradictionException on the load[bin] variable
     */
    private boolean filterLoadInf(int dim, int bin, int newLoadInf) throws ContradictionException {
        int inc = newLoadInf - loads[dim][bin].getLB();
        if (inc > 0) {
            loads[dim][bin].updateLowerBound(newLoadInf, aCause);
            int r = sumLoadInf[dim].add(inc);
            if (sumISizes[dim] < r) {
                contradiction(null, "");
            }
            return true;
        }
        return false;
    }

    /**
     * decrease the UB of the bin load and the sum of the bin load UBs
     *
     * @param bin        bin index
     * @param newLoadSup new UB of the bin load
     * @return {@code true} if UB is decreased.
     * @throws solver.exception.ContradictionException on the load[bin] variable
     */
    private boolean filterLoadSup(int dim, int bin, int newLoadSup) throws ContradictionException {
        int dec = newLoadSup - loads[dim][bin].getUB();
        if (dec < 0) {
            loads[dim][bin].updateUpperBound(newLoadSup, aCause);
            int r = sumLoadSup[dim].add(dec);
            if (sumISizes[dim] > r) {
                contradiction(null, "");
            }
            return true;
        }
        return false;
    }

    //****************************************************************//
    //********* Checkers *********************************************//
    //****************************************************************//

    /**
     * Check the consistency of the required and candidate loads with regards to the assignment variables:
     * for each bin: sumAssignedItemSizes == binRequiredLoad, sumAllPossibleItemSizes == binTotalLoad
     * rule 3, for each bin: binRequiredLoad <= binLoad <= binTotalLoad
     *
     * @return {@code false} if not consistent.
     */
    private boolean checkLoadConsistency() {
        boolean check = true;
        int[][] rs = new int[nbDims][nbBins];
        int[][] cs = new int[nbDims][nbBins];
        for (int i = 0; i < bins.length; i++) {
            if (bins[i].instantiated()) {
                for (int d = 0; d < nbDims; d++) {
                    rs[d][bins[i].getValue()] += iSizes[d][i];
                }
            } else {
                DisposableValueIterator it = bins[i].getValueIterator(true);
                try {
                    while (it.hasNext()) {
                        int v = it.next();
                        for (int d = 0; d < nbDims; d++) {
                            cs[d][v] += iSizes[d][i];
                        }
                    }
                } finally {
                    it.dispose();
                }
            }
        }

        for (int d = notEntailedDims.nextSetBit(0); d >= 0; d = notEntailedDims.nextSetBit(d + 1)) {
            int sli = 0;
            int sls = 0;
            for (int b = 0; b < rs[d].length; b++) {
                if (rs[d][b] != bRLoads[d][b].get()) {
                    LOGGER.warn(name[d] + ": " + loads[d][b].toString() + " required=" + bRLoads[d][b].get() + " expected=" + Arrays.toString(rs[b]));
                    check = false;
                }
                if (rs[d][b] + cs[d][b] != bTLoads[d][b].get()) {
                    LOGGER.warn(name[d] + ": " + loads[d][b].toString() + " total=" + bTLoads[d][b].get() + " expected=" + (rs[d][b] + cs[d][b]));
                    check = false;
                }
                if (loads[d][b].getLB() < rs[d][b]) {
                    LOGGER.warn(name[d] + ": " + loads[d][b].toString() + " LB expected >=" + rs[d][b]);
                    check = false;
                }
                if (loads[d][b].getUB() > rs[d][b] + cs[d][b]) {
                    LOGGER.warn(name[d] + ": " + loads[d][b].toString() + " UB expected <=" + (rs[d][b] + cs[d][b]));
                    check = false;
                }
                sli += loads[d][b].getLB();
                sls += loads[d][b].getUB();
            }
            if (this.sumLoadInf[d].get() != sli) {
                LOGGER.warn(name[d] + ": " + "Sum Load LB = " + this.sumLoadInf[d].get() + " expected =" + sli);
                check = false;
            }
            if (this.sumLoadSup[d].get() != sls) {
                LOGGER.warn(name[d] + ": " + "Sum Load UB = " + this.sumLoadSup[d].get() + " expected =" + sls);
                check = false;
            }
            if (!check) {
                for (int b = 0; b < rs[d].length; b++) {
                    LOGGER.error(name[d] + ": " + loads[d][b].toString() + " required=" + bRLoads[d][b].get() + " (" + rs[d][b] + ") total=" + bTLoads[d][b].get() + " (" + (rs[d][b] + cs[d][b]) + ")");
                }
                LOGGER.error(name[d] + ": " + "Sum Load LB = " + this.sumLoadInf[d].get() + " (" + sumLoadInf[d] + ")");
                LOGGER.error(name[d] + ": " + "Sum Load UB = " + this.sumLoadSup[d].get() + " (" + sumLoadSup[d] + ")");
                for (IntVar v : bins) {
                    LOGGER.info(v.toString());
                }
            }
        }
        return check;
    }
}