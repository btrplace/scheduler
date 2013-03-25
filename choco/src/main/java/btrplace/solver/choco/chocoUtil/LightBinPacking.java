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

import choco.Choco;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
     * The number of items that can be placed on each bin.
     */
    private IStateInt[] candidates;

    /**
     * The number of bins.
     */
    private final int nbBins;

    /**
     * The bin assigned to each item.
     */
    private final IntDomainVar[] bins;

    /**
     * The constant size of each item in decreasing order.
     */
    private final int[] iSizes;

    private IntDomainVar[] sizes;

    /**
     * The sum of the item sizes.
     */
    private long sumISizes;

    /**
     * The load of each bin.
     */
    private final IntDomainVar[] loads;

    /**
     * The total size of the required + candidate items for each bin.
     */
    private IStateInt[] bTLoads;

    /**
     * The total size of the required items for each bin.
     */
    private IStateInt[] bRLoads;

    /**
     * The sum of the bin load LBs.
     */
    private IStateInt sumLoadInf;

    /**
     * The sum of the bin load UBs.
     */
    private IStateInt sumLoadSup;

    /**
     * Has some bin load variable changed since the last propagation ?
     */
    private IStateBool loadsHaveChanged;

    /**
     * provide the items index in iSizes from its position in the bitset.
     */
    private int[] bsToVars;

    /**
     * Provide the item position in the bitset from its position in iSizes.
     */
    private int[] varsToBs;

    /**
     * The bins having candidate items.
     */
    private IStateBitSet availableBins;

    /**
     * constructor of the FastBinPacking global constraint
     *
     * @param environment the solver environment
     * @param loads       array of nbBins variables, each figuring the total size of the items assigned to it, usually initialized to [0, capacity]
     * @param sizes       array of nbItems variables, each figuring the item size. Only the LB will be considered!
     * @param bins        array of nbItems variables, each figuring the possible bins an item can be assigned to, usually initialized to [0, nbBins-1]
     */
    public LightBinPacking(IEnvironment environment, IntDomainVar[] loads, IntDomainVar[] sizes, IntDomainVar[] bins) {
        super(ArrayUtils.append(bins, loads));

        this.env = environment;
        this.loads = loads;
        this.nbBins = loads.length;
        this.bins = bins;
        this.iSizes = new int[sizes.length];
        this.sizes = sizes;

        this.bTLoads = new IStateInt[nbBins];
        this.bRLoads = new IStateInt[nbBins];
    }

    public final int getRemainingSpace(int bin) {
        return loads[bin].getSup() - bRLoads[bin].get();
    }

    private void sortIndices() {
        long sum = 0;
        //l denotes the ordering of the items, so bsToVars
        List<Integer> l = new ArrayList<Integer>(iSizes.length);
        for (int i = 0; i < iSizes.length; i++) {
            l.add(i);
            iSizes[i] = sizes[i].getInf();
            sum += iSizes[i];
        }
        this.sumISizes = sum;
        Collections.sort(l, new Comparator<Integer>() {
            @Override
            public int compare(Integer i1, Integer i2) {
                return iSizes[i2] - iSizes[i1];
            }
        });
        bsToVars = new int[iSizes.length];
        varsToBs = new int[iSizes.length];
        for (int i = 0; i < bsToVars.length; i++) {
            bsToVars[i] = l.get(i);
            varsToBs[l.get(i)] = i;
        }
    }

    @Override
    public boolean isConsistent() {
        int[] l = new int[nbBins];
        int[] c = new int[nbBins];
        for (int i = 0; i < bins.length; i++) { //Assignment variable
            if (bins[i].isInstantiated()) {
                int v = bins[i].getVal();
                l[v] += iSizes[i];
                if (l[v] > loads[v].getSup()) {
                    return false;
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
        //return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
        return IntVarEvent.BOUNDS_MASK;
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        int[] l = new int[nbBins];
        int[] c = new int[nbBins];
        for (int i = 0; i < bins.length; i++) {
            final int b = tuple[i];
            l[b] += iSizes[i];
            c[b]++;
        }
        for (int b = 0; b < nbBins; b++) {
            if (tuple[b + bins.length] != l[b]) {
                ChocoLogging.getBranchingLogger().warning("Bad load of " + b + " = " + tuple[b + bins.length] + " expected =" + l[b]);
                return false;
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

        sortIndices();
        availableBins = env.makeBitSet(nbBins);
        int[] rLoads = new int[nbBins];
        int[] cLoads = new int[nbBins];

        candidates = new IStateInt[nbBins];
        for (int i = 0; i < nbBins; i++) {
            candidates[i] = env.makeInt(0);
        }
        for (int i = 0; i < bins.length; i++) {
            bins[i].updateInf(0, this, false);
            bins[i].updateSup(nbBins - 1, this, false);
            if (bins[i].isInstantiated()) {
                rLoads[bins[i].getVal()] += iSizes[i];
            } else {
                DisposableIntIterator it = bins[i].getDomain().getIterator();
                try {
                    while (it.hasNext()) {
                        int b = it.next();
                        candidates[b].add(1);
                        cLoads[b] += iSizes[i];
                    }
                } finally {
                    it.dispose();
                }
            }
        }
        long ed = System.currentTimeMillis();

        int slb = 0;
        int slu = 0;
        for (int b = 0; b < nbBins; b++) {
            bRLoads[b] = env.makeInt(rLoads[b]);
            bTLoads[b] = env.makeInt(rLoads[b] + cLoads[b]);
            loads[b].updateInf(rLoads[b], this, false);
            loads[b].updateSup(rLoads[b] + cLoads[b], this, false);
            if (candidates[b].get() > 0) {
                availableBins.set(b);
            }
            slb += loads[b].getInf();
            slu += loads[b].getSup();
        }

        this.sumLoadInf = env.makeInt(slb);
        this.sumLoadSup = env.makeInt(slu);
        this.loadsHaveChanged = env.makeBool(false);

        assert checkLoadConsistency();
        int minRemaining = Choco.MAX_UPPER_BOUND;
        for (int i = 0; i < loads.length; i++) {
            int m = getRemainingSpace(i);
            if (m < minRemaining) {
                minRemaining = m;
            }
        }
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

        boolean noFixPoint = true;
        while (noFixPoint) {
            if (sumISizes > sumLoadSup.get() || sumISizes < sumLoadInf.get()) {
                fail();
            }
            noFixPoint = false;
            for (int b = availableBins.nextSetBit(0); b >= 0; b = availableBins.nextSetBit(b + 1)) {
                noFixPoint |= filterLoadInf(b, Math.max(bRLoads[b].get(), (int) sumISizes - sumLoadSup.get() + loads[b].getSup()));
                noFixPoint |= filterLoadSup(b, Math.min(bTLoads[b].get(), (int) sumISizes - sumLoadInf.get() + loads[b].getInf()));
            }
        }
        assert checkLoadConsistency();
    }

    /**
     * recompute the sum of the min/max loads only if at least one variable bound has been updated outside the constraint
     */
    private void recomputeLoadSums() {
        if (!loadsHaveChanged.get()) {
            return;
        }
        loadsHaveChanged.set(false);
        int sli = 0;
        int sls = 0;
        for (int b = 0; b < nbBins; b++) {
            sli += loads[b].getInf();
            sls += loads[b].getSup();
        }

        this.sumLoadInf.set(sli);
        this.sumLoadSup.set(sls);
    }

    /**
     * on loads variables: delay propagation
     */
    @Override
    public void awakeOnInf(int varIdx) throws ContradictionException {
        loadsHaveChanged.set(true);
    }

    /**
     * on loads variables: delay propagation
     */
    @Override
    public void awakeOnSup(int varIdx) throws ContradictionException {
        loadsHaveChanged.set(true);
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
                removeItem(varsToBs[iIdx], deltaDomain.next());
            }
        } finally {
            deltaDomain.dispose();
        }
        if (vars[iIdx].getInf() == vars[iIdx].getSup()) {
            assignItem(varsToBs[iIdx], vars[iIdx].getVal());
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
        int r = bRLoads[bin].add(iSizes[bsToVars[item]]);
        filterLoadInf(bin, r);
        int v = candidates[bin].add(-1);
        if (v == 0) {
            availableBins.clear(bin);
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
        //if (candidates[bin].get(item)) {
        int v = candidates[bin].add(-1);
        if (v == 0) {
            availableBins.clear(bin);
        }
        int r = bTLoads[bin].add(-1 * iSizes[bsToVars[item]]);
        filterLoadSup(bin, r);
        //}
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
    private boolean filterLoadInf(int bin, int newLoadInf) throws ContradictionException {
        int inc = newLoadInf - loads[bin].getInf();
        if (inc > 0) {
            loads[bin].updateInf(newLoadInf, this, false);
            int r = sumLoadInf.add(inc);
            if (sumISizes < r) {
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
    private boolean filterLoadSup(int bin, int newLoadSup) throws ContradictionException {
        int dec = newLoadSup - loads[bin].getSup();
        if (dec < 0) {
            loads[bin].updateSup(newLoadSup, this, false);
            int r = sumLoadSup.add(dec);
            if (sumISizes > r) {
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
        int[] rs = new int[nbBins];
        int[] cs = new int[nbBins];
        for (int i = 0; i < bins.length; i++) {
            if (bins[i].isInstantiated()) {
                rs[bins[i].getVal()] += iSizes[i];
            } else {
                DisposableIntIterator it = bins[i].getDomain().getIterator();
                try {
                    while (it.hasNext()) {
                        cs[it.next()] += iSizes[i];
                    }
                } finally {
                    it.dispose();
                }
            }
        }
        int sli = 0;
        int sls = 0;
        for (int b = 0; b < rs.length; b++) {
            if (rs[b] != bRLoads[b].get()) {
                ChocoLogging.getBranchingLogger().warning(loads[b].pretty() + " required=" + bRLoads[b].get() + " expected=" + rs[b]);
                check = false;
            }
            if (rs[b] + cs[b] != bTLoads[b].get()) {
                ChocoLogging.getBranchingLogger().warning(loads[b].pretty() + " total=" + bTLoads[b].get() + " expected=" + (rs[b] + cs[b]));
                check = false;
            }
            if (loads[b].getInf() < rs[b]) {
                ChocoLogging.getBranchingLogger().warning(loads[b].pretty() + " LB expected >=" + rs[b]);
                check = false;
            }
            if (loads[b].getSup() > rs[b] + cs[b]) {
                ChocoLogging.getBranchingLogger().warning(loads[b].pretty() + " UB expected <=" + (rs[b] + cs[b]));
                check = false;
            }
            sli += loads[b].getInf();
            sls += loads[b].getSup();
        }
        if (this.sumLoadInf.get() != sli) {
            ChocoLogging.getBranchingLogger().warning("Sum Load LB = " + this.sumLoadInf.get() + " expected =" + sli);
            check = false;
        }
        if (this.sumLoadSup.get() != sls) {
            ChocoLogging.getBranchingLogger().warning("Sum Load UB = " + this.sumLoadSup.get() + " expected =" + sls);
            check = false;
        }
        ChocoLogging.flushLogs();
        if (!check) {
            for (int b = 0; b < rs.length; b++) {
                ChocoLogging.getBranchingLogger().severe(loads[b].pretty() + " required=" + bRLoads[b].get() + " (" + rs[b] + ") total=" + bTLoads[b].get() + " (" + (rs[b] + cs[b]) + ")");
            }
            ChocoLogging.getBranchingLogger().severe("Sum Load LB = " + this.sumLoadInf.get() + " (" + sumLoadInf + ")");
            ChocoLogging.getBranchingLogger().severe("Sum Load UB = " + this.sumLoadSup.get() + " (" + sumLoadSup + ")");
            for (int i = 0; i < bins.length; i++) {
                ChocoLogging.getBranchingLogger().severe(bins[i].pretty());
            }
        }
        return check;
    }
}