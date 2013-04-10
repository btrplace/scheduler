/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Arrays;

/**
 * Lighter but faster version of {@link BinPacking} that does not provide the knapsack filtering
 *
 * @author Fabien Hermenier
 */
public class LightBinPacking extends AbstractLargeIntSConstraint {

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
    private final IntDomainVar[] bins;

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
    private final IntDomainVar[][] loads;

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

//    private IStateInt[] candidates;

    /**
     * constructor of the FastBinPacking global constraint
     *
     * @param environment the solver environment
     * @param loads       array of nbBins variables, each figuring the total size of the items assigned to it, usually initialized to [0, capacity]
     * @param sizes       array of nbItems variables, each figuring the item size. Only the LB will be considered!
     * @param bins        array of nbItems variables, each figuring the possible bins an item can be assigned to, usually initialized to [0, nbBins-1]
     */
    public LightBinPacking(String[] name, IEnvironment environment, IntDomainVar[][] loads, int[][] sizes, IntDomainVar[] bins) {
        super(ArrayUtils.append(bins, ArrayUtils.flatten(loads)));
        this.name = name;
        this.env = environment;
        this.loads = loads;
        this.nbBins = loads[0].length;
        this.nbDims = loads.length;
        this.bins = bins;
        this.iSizes = sizes;
        this.bTLoads = new IStateInt[nbDims][nbBins];
        this.bRLoads = new IStateInt[nbDims][nbBins];
    }

    @Override
    public boolean isConsistent() {
        int[][] l = new int[nbDims][nbBins];
        for (int i = 0; i < bins.length; i++) { //Assignment variable
            if (bins[i].isInstantiated()) {
                for (int d = 0; d < nbDims; d++) {
                    int v = bins[i].getVal();
                    l[d][v] += iSizes[d][i];
                    if (l[d][v] > loads[d][v].getSup()) {
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
    public int getFilteredEventMask(int idx) {
        if (idx < bins.length) {
            return IntVarEvent.REMVAL_MASK;
        }
        return IntVarEvent.BOUNDS_MASK;
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
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
            //for (int d = 0; d < nbDims; d++) {
            for (int d = notEntailedDims.nextSetBit(0); d >= 0; d = notEntailedDims.nextSetBit(d + 1)) {
                int loadPos = iSizes[0].length + d * nbBins + b;
                if (tuple[loadPos] != l[d][b]) {
                    ChocoLogging.getBranchingLogger().warning("Invalid load for bin " + b + " on dimension " + d + ". Was " + tuple[loadPos] + ", expected " + l[d][b]);
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * initialize the internal data: availableBins, candidates, binRequiredLoads, binTotalLoads, sumLoadInf, sumLoadSup
     * shrink the item-to-bins assignment variables: 0 <= bins[i] <= nbBins
     * shrink the bin load variables: binRequiredLoad <= binLoad <= binTotalLoad
     */
    @Override
    public void awake() throws ContradictionException {

        sumISizes = new long[nbDims];
        notEntailedDims = env.makeBitSet(nbDims);
        notEntailedDims.clear(0, 3);
        for (int d = 0; d < nbDims; d++) {
            long sum = 0;
            for (int i = 0; i < iSizes[d].length; i++) {
                sum += iSizes[d][i];
            }
            this.sumISizes[d] = sum;
        }

        int[][] rLoads = new int[nbDims][nbBins];
        int[][] cLoads = new int[nbDims][nbBins];

        int[] nbUnassigned = new int[nbDims];
        int[] cs = new int[nbBins];
        for (int i = 0; i < bins.length; i++) {
            bins[i].updateInf(0, this, false);
            bins[i].updateSup(nbBins - 1, this, false);
            if (bins[i].isInstantiated()) {
                for (int d = 0; d < nbDims; d++) {
                    rLoads[d][bins[i].getVal()] += iSizes[d][i];
                }
            } else {
                for (int d = 0; d < nbDims; d++) {
                    nbUnassigned[d] += iSizes[d][i];
                }
                DisposableIntIterator it = bins[i].getDomain().getIterator();
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
                loads[d][b].updateInf(rLoads[d][b], this, false);
                loads[d][b].updateSup(rLoads[d][b] + cLoads[d][b], this, false);
                slb[d] += loads[d][b].getInf();
                slu[d] += loads[d][b].getSup();
            }
        }

        sumLoadInf = new IStateInt[nbDims];
        sumLoadSup = new IStateInt[nbDims];
        for (int d = 0; d < nbDims; d++) {
            this.sumLoadInf[d] = env.makeInt(slb[d]);
            this.sumLoadSup[d] = env.makeInt(slu[d]);
        }

        this.loadsHaveChanged = env.makeBool(false);

        for (int d = 0; d < nbDims; d++) {
            for (int b = 0; b < nbBins; b++) {
                if (loads[d][b].getSup() - loads[d][b].getInf() < nbUnassigned[d]) {
                    ChocoLogging.getBranchingLogger().info(sumISizes[d] + " >= ub(" + loads[d][b].pretty() + ")" + cLoads[d][b]);
                    notEntailedDims.set(d);
                    break;
                }
                //notEntailedDims.set(d);
            }
        }
        assert checkLoadConsistency();
        ChocoLogging.getBranchingLogger().info(Arrays.toString(name) + " " + Arrays.toString(cLoads) + " notEntailed: " + notEntailedDims);
        propagate();
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
    public void propagate() throws ContradictionException {
        recomputeLoadSums();
        for (int d = 0; d < nbDims; d++) {
            //for (int d = notEntailedDims.nextSetBit(0); d >= 0; d = notEntailedDims.nextSetBit(d + 1)) {
            if (sumISizes[d] > sumLoadSup[d].get() || sumISizes[d] < sumLoadInf[d].get()) {
                fail();
            }
        }

            /*for (int b = 0; b < nbBins; b++) {
                for (int d = notEntailedDims.nextSetBit(0); d >= 0; d = notEntailedDims.nextSetBit(d + 1)) {
                    filterLoadInf(d, b, Math.max(bRLoads[d][b].get(), (int) sumISizes[d] - sumLoadSup[d].get() + loads[d][b].getSup()));
                    filterLoadSup(d, b, Math.min(bTLoads[d][b].get(), (int) sumISizes[d] - sumLoadInf[d].get() + loads[d][b].getInf()));
                }
            } */
        assert checkLoadConsistency();
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
                sli += loads[d][b].getInf();
                sls += loads[d][b].getSup();
            }

            this.sumLoadInf[d].set(sli);
            this.sumLoadSup[d].set(sls);
        }
        return true;
    }

    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void awakeOnBounds(int varIndex) throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void awakeOnRem(int varIdx, int val) throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    /**
     * on loads variables: delay propagation
     */
    @Override
    public void awakeOnInf(int varIdx) throws ContradictionException {
        loadsHaveChanged.set(true);
        constAwake(false);
    }

    /**
     * on loads variables: delay propagation
     */
    @Override
    public void awakeOnSup(int varIdx) throws ContradictionException {
        loadsHaveChanged.set(true);
        constAwake(false);
    }


    /**
     * on bins variables: propagate the removal of item-to-bins assignments.
     * 1) update the candidate and check to decrease the load UB of each removed bins: binLoad <= binTotalLoad
     * 2) if item is assigned: update the required and check to increase the load LB of the bin: binLoad >= binRequiredLoad
     *
     * @throws choco.kernel.solver.ContradictionException
     *          on the load variables
     */
    @Override
    public void awakeOnRemovals(int iIdx, DisposableIntIterator deltaDomain) throws ContradictionException {
        try {
            while (deltaDomain.hasNext()) {
                int b = deltaDomain.next();
                /*int r = candidates[b].decrement();
                if (r == 0) {
                    for (int d = 0; d < nbDims; d++) {
                        loads[d][b].instantiate(loads[d][b].getInf(), this, false);
                    }
                } */
                removeItem(iIdx, b);
            }

        } finally {
            deltaDomain.dispose();
        }
        if (vars[iIdx].getInf() == vars[iIdx].getSup()) {
            assignItem(iIdx, vars[iIdx].getVal());
        }
        this.constAwake(false);
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
     * @throws choco.kernel.solver.ContradictionException
     *          on the load[bin] variable
     */
    private void assignItem(int item, int bin) throws ContradictionException {
        //for (int d = notEntailedDims.nextSetBit(0); d >= 0; d = notEntailedDims.nextSetBit(d + 1)) {
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
     * @throws choco.kernel.solver.ContradictionException
     *          on the load[bin] variable
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
     * @throws choco.kernel.solver.ContradictionException
     *          on the load[bin] variable
     */
    private boolean filterLoadInf(int dim, int bin, int newLoadInf) throws ContradictionException {
        int inc = newLoadInf - loads[dim][bin].getInf();
        if (inc > 0) {
            loads[dim][bin].updateInf(newLoadInf, this, false);
            int r = sumLoadInf[dim].add(inc);
            if (sumISizes[dim] < r) {
                fail();
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
     * @throws choco.kernel.solver.ContradictionException
     *          on the load[bin] variable
     */
    private boolean filterLoadSup(int dim, int bin, int newLoadSup) throws ContradictionException {
        int dec = newLoadSup - loads[dim][bin].getSup();
        if (dec < 0) {
            loads[dim][bin].updateSup(newLoadSup, this, false);
            int r = sumLoadSup[dim].add(dec);
            if (sumISizes[dim] > r) {
                fail();
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
            if (bins[i].isInstantiated()) {
                for (int d = 0; d < nbDims; d++) {
                    rs[d][bins[i].getVal()] += iSizes[d][i];
                }
            } else {
                DisposableIntIterator it = bins[i].getDomain().getIterator();
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

        //for (int d = 0; d < nbDims; d++) {
        for (int d = notEntailedDims.nextSetBit(0); d >= 0; d = notEntailedDims.nextSetBit(d + 1)) {
            int sli = 0;
            int sls = 0;
            for (int b = 0; b < rs[d].length; b++) {
                if (rs[d][b] != bRLoads[d][b].get()) {
                    ChocoLogging.getBranchingLogger().warning(name[d] + ": " + loads[d][b].pretty() + " required=" + bRLoads[d][b].get() + " expected=" + rs[b]);
                    check = false;
                }
                if (rs[d][b] + cs[d][b] != bTLoads[d][b].get()) {
                    ChocoLogging.getBranchingLogger().warning(name[d] + ": " + loads[d][b].pretty() + " total=" + bTLoads[d][b].get() + " expected=" + (rs[d][b] + cs[d][b]));
                    check = false;
                }
                if (loads[d][b].getInf() < rs[d][b]) {
                    ChocoLogging.getBranchingLogger().warning(name[d] + ": " + loads[d][b].pretty() + " LB expected >=" + rs[d][b]);
                    check = false;
                }
                if (loads[d][b].getSup() > rs[d][b] + cs[d][b]) {
                    ChocoLogging.getBranchingLogger().warning(name[d] + ": " + loads[d][b].pretty() + " UB expected <=" + (rs[d][b] + cs[d][b]));
                    check = false;
                }
                sli += loads[d][b].getInf();
                sls += loads[d][b].getSup();
            }
            if (this.sumLoadInf[d].get() != sli) {
                ChocoLogging.getBranchingLogger().warning(name[d] + ": " + "Sum Load LB = " + this.sumLoadInf[d].get() + " expected =" + sli);
                check = false;
            }
            if (this.sumLoadSup[d].get() != sls) {
                ChocoLogging.getBranchingLogger().warning(name[d] + ": " + "Sum Load UB = " + this.sumLoadSup[d].get() + " expected =" + sls);
                check = false;
            }
            ChocoLogging.flushLogs();
            if (!check) {
                for (int b = 0; b < rs[d].length; b++) {
                    ChocoLogging.getBranchingLogger().severe(name[d] + ": " + loads[d][b].pretty() + " required=" + bRLoads[d][b].get() + " (" + rs[d][b] + ") total=" + bTLoads[d][b].get() + " (" + (rs[d][b] + cs[d][b]) + ")");
                }
                ChocoLogging.getBranchingLogger().severe(name[d] + ": " + "Sum Load LB = " + this.sumLoadInf[d].get() + " (" + sumLoadInf[d] + ")");
                ChocoLogging.getBranchingLogger().severe(name[d] + ": " + "Sum Load UB = " + this.sumLoadSup[d].get() + " (" + sumLoadSup[d] + ")");
                for (int i = 0; i < bins.length; i++) {
                    ChocoLogging.getBranchingLogger().info(bins[i].pretty());
                }
            }
        }
        return check;
    }
}