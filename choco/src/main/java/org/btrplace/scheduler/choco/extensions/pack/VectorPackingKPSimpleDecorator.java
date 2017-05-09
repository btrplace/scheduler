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

import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.memory.structure.S64BitSet;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.iterators.DisposableValueIterator;

import java.util.ArrayList;

/**
 * An optional extension of VectorPacking allowing knapsack process.
 * - when an item is assigned to a bin and if then assignedLoad == sup(binLoad)
 *   for this bin then remove all candidate items from this bin
 * - when an item is assigned to a bin then all the candidate items that are
 *   too big to fit the new remaining space are filtered out.
 * @author Sophie Demassey
 */
public class VectorPackingKPSimpleDecorator {

    /**
     * the core BinPacking propagator
     */
    private VectorPackingPropagator prop;

    /**
     * Track the biggest item usage per dimension and bin.
     */
    private final IStateInt[][] dynBiggest;

    /**
     * the list of candidate items for each bin [nbBins][]
     */
    protected ArrayList<IStateBitSet> candidate;

    public VectorPackingKPSimpleDecorator(VectorPackingPropagator p) {
        this.prop = p;
        this.candidate = new ArrayList<>(p.nbBins);
        for (int i = 0; i < p.nbBins; i++) {
            candidate.add(new S64BitSet(p.getModel().getEnvironment(), p.bins.length));
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
     * initialize the lists of candidates.
     */
    protected void postInitialize() throws ContradictionException {
        final int[] biggest = new int[prop.nbDims];
        for (int i = 0; i < prop.bins.length; i++) {
            for (int d = 0; d < prop.nbDims; d++) {
                biggest[d] = Math.max(biggest[d], prop.iSizes[d][i]);
            }
            if (!prop.bins[i].isInstantiated()) {
                final DisposableValueIterator it = prop.bins[i].getValueIterator(true);
                try {
                    while (it.hasNext()) {
                        candidate.get(it.next()).set(i);
                    }
                } finally {
                    it.dispose();
                }
            }
        }

        for (int b = 0; b < prop.nbBins; b++) {
            for (int d = 0; d < prop.nbDims; d++) {
                dynBiggest[d][b] =
                        prop.getVars()[0].getEnvironment().makeInt(biggest[d]);
            }
        }

        fullKnapsack();
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
            // ISSUE 86: the event 'i removed from bin' can already been in the propagation stack but not yet considered
            // ie. !prop.bins[i].contains(bin) && candidate[bin].contains(i): in this case, do not process it yet
            if (prop.iSizes[dim][i] == 0) {
                continue;
            }
            if (prop.bins[i].removeValue(bin, prop)) {
                candidate.get(bin).clear(i);
                prop.potentialLoad[dim][bin].add(-prop.iSizes[dim][i]);
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
     */
    @SuppressWarnings("squid:S3346")
    protected void postRemoveItem(int item, int bin) {
        assert candidate.get(bin).get(item);
        candidate.get(bin).clear(item);
    }

    /**
     * update the candidate list of a bin when an item is assigned
     * then apply the full bin filter if sup(binLoad) is reached
     * this function may be recursive
     *
     * @param item the assigned item
     * @param bin  the bin
     * @throws ContradictionException
     */
    protected void postAssignItem(int item, int bin) throws
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
     * @param bin the bin
     * @param d the dimension
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
