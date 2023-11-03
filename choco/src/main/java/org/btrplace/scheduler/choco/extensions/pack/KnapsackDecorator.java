/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions.pack;

import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.exception.ContradictionException;

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
    protected IStateBitSet[] candidate;

    /**
     * the core BinPacking propagator
     */
    private final VectorPackingPropagator prop;

    public KnapsackDecorator(VectorPackingPropagator p) {
        this.prop = p;
        this.candidate = new IStateBitSet[p.nbBins];

        for (int i = 0; i < p.nbBins; i++) {
            final IStateBitSet bs = new org.chocosolver.memory.structure.SparseBitSet(p.getModel().getEnvironment(), 256);
            candidate[i] = bs;
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
            if (prop.bins[i].isInstantiated()) {
                // Instantiated, candidate for no bin.
                for (int b = 0; b < prop.nbBins; b++) {
                    candidate[b].clear(i);
                }
            } else {
                if (prop.bins[i].getDomainSize() == prop.nbBins) {
                    // Can go everywhere. No holes to do in the bitset.
                    continue;
                }
                // We only put holes in the bitset for the non-candidate items.
                for (int b = 0; b < prop.nbBins; b++) {
                    if (!prop.bins[i].contains(b)) {
                        candidate[b].clear(i);
                    }
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
        // Count the number of candidates per item.
        double sumCandidates = 0;
        for (int i = 0; i < prop.bins.length; i++) {
            sumCandidates += prop.bins[i].getDomainSize();
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

        double candidatesRatio = sumCandidates / (this.prop.nbBins * this.prop.bins.length);
        if (candidatesRatio > 0.5) {
            // On average, items have more than half the bins as candidates. Let's consider they are candidate for every
            // bin, then create holes accordingly.
            postInitializeOpen();
        } else {
            // Opposite reasoning. Let's consider they are candidate for nothing and set bits according to their domain.
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
        // Iterate in descending order to set the bitset size properly in one shot.
        for (int i = prop.bins.length - 1; i >= 0; i--) {
            if (prop.bins[i].isInstantiated()) {
                continue;
            }
            int ub = prop.bins[i].getUB();
            for (int v = prop.bins[i].getLB(); v <= ub; v = prop.bins[i].nextValue(v)) {
                // operate on value i here
                candidate[v].set(i);
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

        final IStateBitSet cc = candidate[bin];
        for (int i = cc.nextSetBit(0); i >= 0; i = cc.nextSetBit(i + 1)) {
            if (prop.iSizes[dim][i] == 0) {
                continue;
            }
            // ISSUE 86: the event 'i removed from bin' can already been in the propagation stack but not yet considered
            // ie. !prop.bins[i].contains(bin) && candidate[bin].contains(i): in this case, do not process it yet
            if (prop.bins[i].removeValue(bin, prop)) {
                cc.clear(i);
                prop.updateLoads(i, bin);

                if (prop.bins[i].isInstantiated()) {
                    prop.assignItem(i, prop.bins[i].getValue());
                }
            }
        }
        if (cc.isEmpty()) {
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
        final IStateBitSet cand = candidate[bin];
        if (!cand.get(item)) {
            return false;
        }
        cand.clear(item);
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
        final IStateBitSet cc = candidate[bin];
        if (!cc.get(item)) {
            return;
        }
        cc.clear(item);

        for (int d = 0; d < prop.nbDims; d++) {
            knapsack(bin, d);

            // The bin is full. We get rid of every candidate and set the bin load.
            if (prop.loads[d][bin].getUB() == prop.assignedLoad[d][bin].get()) {
                filterFullDim(bin, d);
                if (cc.isEmpty()) {
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
            final IStateBitSet cc = candidate[bin];
            for (int i = cc.nextSetBit(0); i >= 0;
                 i = cc.nextSetBit(i + 1)) {
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
