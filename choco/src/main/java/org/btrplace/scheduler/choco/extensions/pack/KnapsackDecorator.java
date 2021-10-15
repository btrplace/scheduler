/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions.pack;

import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.memory.structure.S64BitSet;
import org.chocosolver.solver.exception.ContradictionException;

import java.util.ArrayList;

/**
 * An optional extension of VectorPacking allowing knapsack process.
 * - when an item is assigned to a bin and if then assignedLoad == sup(binLoad)
 * for this bin then remove all candidate items from this bin
 * - when an item is assigned to a bin then all the candidate items that are
 * too big to fit the new remaining space are filtered out.
 * <p>
 * This decorator is called every time an item is assigned to a bin or removed from a bin.
 *
 * @author Sophie Demassey
 */
public class KnapsackDecorator {

    /**
     * Track the biggest item usage per dimension and bin.
     */
    private final IStateInt[][] dynBiggest;
    /**
     * the list of candidate items for each bin [nbBins][]
     */
    protected ArrayList<IStateBitSet> candidate;
    /**
     * the core BinPacking propagator
     */
    private final VectorPackingPropagator prop;

    public KnapsackDecorator(VectorPackingPropagator p) {
        this.prop = p;
        this.candidate = new ArrayList<>(p.nbBins);
        for (int i = 0; i < p.nbBins; i++) {
            final IStateBitSet bs = new S64BitSet(p.getModel().getEnvironment(), p.bins.length);
            candidate.add(bs);
        }

        dynBiggest = new IStateInt[prop.nbDims][prop.nbBins];
    }

    /**
     * Propagate a knapsack on every node and every dimension.
     *
     * @throws ContradictionException
     */
    private void fullKnapsack() throws ContradictionException {
        for (int bin = 0; bin < prop.nbBins; bin++) {
            for (int d = 0; d < prop.nbDims; d++) {
                knapsack(bin, d);
            }
        }
    }

    /**
     * Initialize the lists of candidates by considering that items are candidate
     * for every bin by default.
     */
    private void postInitializeOpen() {

        // By default, the items are candidate for every bin.
        for (final IStateBitSet bs : candidate) {
            bs.set(0, prop.bins.length);
        }

        for (int i = 0; i < prop.bins.length; i++) {
            if (!prop.bins[i].isInstantiated()) {
                // We only put holes in the bitset if for sure some items can't
                // can't go on some bins.
                if (prop.bins[i].getDomainSize() != prop.nbBins) {
                    for (int b = 0; b < prop.nbBins; b++) {
                        if (!prop.bins[i].contains(b)) {
                            candidate.get(b).clear(i);
                        }
                    }
                }
            } else {
                // Instantiated, candidate for no bin.
                for (int b = 0; b < prop.nbBins; b++) {
                    candidate.get(b).clear(i);
                }
            }
        }
    }

    /**
     * initialize the lists of candidates.
     *
     * @throws ContradictionException if a contradiction occurs.
     */
    public void postInitialize() throws ContradictionException {

        final int[] biggest = new int[prop.nbDims];
        int nbOpens = 0;
        for (int i = 0; i < prop.bins.length; i++) {
            if (!prop.bins[i].isInstantiated()) {
                nbOpens++;
            }
            for (int d = 0; d < prop.nbDims; d++) {
                biggest[d] = Math.max(biggest[d], prop.iSizes[d][i]);
            }
        }

        for (int b = 0; b < prop.nbBins; b++) {
            for (int d = 0; d < prop.nbDims; d++) {
                dynBiggest[d][b] =
                        prop.getVars()[0].getEnvironment().makeInt(biggest[d]);
            }
        }

        if (nbOpens > prop.bins.length / 2) {
            // More than half the items are candidates for multiple bins,
            postInitializeOpen();
        } else {
            postInitializeClose();
        }
        fullKnapsack();
    }

    /**
     * Initialize the lists of candidates by considering that items are not
     * candidate for any bin by default.
     */
    public void postInitializeClose() {

        // By default, VMs are not candidate for any node.
        for (int i = 0; i < prop.bins.length; i++) {
            if (prop.bins[i].isInstantiated()) {
                continue;
            }
            for (int b = 0; b < prop.nbBins; b++) {
                if (prop.bins[i].contains(b)) {
                    candidate.get(b).set(i);
                }
            }
        }
    }

    /**
     * remove all candidate items from a bin that is full
     * then synchronize potentialLoad and sup(binLoad) accordingly
     * if an item becomes instantiated then propagate the newly assigned bin
     *
     * @param bin the full bin
     * @throws ContradictionException
     */
    @SuppressWarnings("squid:S3346")
    private void filterFullDim(int bin, int dim) throws ContradictionException {

        for (int i = candidate.get(bin).nextSetBit(0); i >= 0; i = candidate.get(bin).nextSetBit(i + 1)) {
            if (prop.iSizes[dim][i] == 0) {
                continue;
            }
            // ISSUE 86: the event 'i removed from bin' can already been in the propagation stack but not yet considered
            // ie. !prop.bins[i].contains(bin) && candidate[bin].contains(i): in this case, do not process it yet
            if (prop.bins[i].removeValue(bin, prop)) {
                candidate.get(bin).clear(i);
                prop.updateLoads(i, bin);

                if (prop.bins[i].isInstantiated()) {
                    prop.assignItem(i, prop.bins[i].getValue());
                }
            }
        }
        if (candidate.get(bin).isEmpty()) {
            assert prop.potentialLoad[dim][bin].get() == prop.assignedLoad[dim][bin].get();
            assert prop.loads[dim][bin].getUB() == prop.potentialLoad[dim][bin].get();
        }
    }

    /**
     * update the candidate list of a bin when an item is removed
     *
     * @param item the removed item
     * @param bin  the bin
     * @return {@code true} if the item was present and has been removed.
     */
    @SuppressWarnings("squid:S3346")
    public boolean postRemoveItem(int item, int bin) {
        if (!candidate.get(bin).get(item)) {
            return false;
        }
        candidate.get(bin).clear(item);
        return true;
    }

    /**
     * update the candidate list of a bin when an item is assigned
     * then apply the full bin filter if sup(binLoad) is reached
     * this function may be recursive
     *
     * @param item the assigned item
     * @param bin  the bin
     * @throws ContradictionException if a contradiction occurs.
     */
    public void postAssignItem(int item, int bin) throws
            ContradictionException {
        if (!candidate.get(bin).get(item)) {
            return;
        }
        candidate.get(bin).clear(item);

        for (int d = 0; d < prop.nbDims; d++) {
            knapsack(bin, d);

            // The bin is full. We get rid of every candidate and set the bin load.
            if (prop.loads[d][bin].getUB() == prop.assignedLoad[d][bin].get()) {
                filterFullDim(bin, d);
                if (candidate.get(bin).isEmpty()) {
                    for (int d2 = 0; d2 < prop.nbDims; d2++) {
                        prop.potentialLoad[d2][bin].set(prop.assignedLoad[d2][bin].get());
                        prop.filterLoadSup(d2, bin, prop.potentialLoad[d2][bin].get());
                    }
                    return;
                }
                return;
            }
        }
    }

    /**
     * Propagate a knapsack on a given dimension and bin.
     * If the usage of an item exceeds the bin free capacity, it is filtered out.
     *
     * @param bin the bin
     * @param d   the dimension
     * @throws ContradictionException
     */
    private void knapsack(int bin, int d) throws ContradictionException {
        final int maxLoad = prop.loads[d][bin].getUB();
        final int free = maxLoad - prop.assignedLoad[d][bin].get();

        if (free >= dynBiggest[d][bin].get()) {
            // fail fast. The remaining space > the biggest item.
            return;
        }
        if (free > 0) {
            // The bin is not full and some items exceeds the remaining space. We
            // get rid of them
            // In parallel, we set the new biggest candidate item for that
            // (bin,dimension)
            int newMax = -1;
            for (int i = candidate.get(bin).nextSetBit(0); i >= 0;
                 i = candidate.get(bin).nextSetBit(i + 1)) {
                if (prop.iSizes[d][i] > free) {
                    if (prop.bins[i].removeValue(bin, prop)) {
                        prop.removeItem(i, bin);
                        if (prop.bins[i].isInstantiated()) {
                            prop.assignItem(i, prop.bins[i].getValue());
                        }
                    }
                } else {
                    // The item is still a candidate, we just update the biggest value.
                    newMax = Math.max(newMax, prop.iSizes[d][i]);
                }
            }
            dynBiggest[d][bin].set(newMax);
        }
    }
}
