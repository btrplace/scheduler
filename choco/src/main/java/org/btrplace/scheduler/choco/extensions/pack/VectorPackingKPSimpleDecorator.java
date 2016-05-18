/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
import org.chocosolver.memory.structure.S64BitSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.iterators.DisposableValueIterator;

import java.util.ArrayList;

/**
 * An optional extension of VectorPacking allowing a very minimal knapsack process :
 * when an item is assigned to a bin and if then assignedLoad == sup(binLoad) for this bin
 * then remove all candidate items from this bin
 * call attachKPSimpleDecorator() to the VectorPackingPropagator object
 *
 * @author Sophie Demassey
 */
public class VectorPackingKPSimpleDecorator {

    /**
     * the core BinPacking propagator
     */
    private VectorPackingPropagator p;

    /**
     * the list of candidate items for each bin [nbBins][]
     */
    private ArrayList<IStateBitSet> candidate;
    // TODO 2: add watchItem[d][b] = the candidate item for b with the maximum size in dimension d
    // when sup(binLoad) or assignedLoad are updated: if assignedLoad + size(watch) > sup(binLoad) then filter the candidates of b


    public VectorPackingKPSimpleDecorator(VectorPackingPropagator p) {
        this.p = p;
        this.candidate = new ArrayList<>(p.nbBins);
        for (int i = 0; i < p.nbBins; i++) {
            candidate.add(new S64BitSet(p.getSolver().getEnvironment(), p.bins.length));
        }
    }


    /**
     * initialize the lists of candidates
     *
     */
    protected void postInitialize() throws ContradictionException{
        for (int i = 0; i < p.bins.length; i++) {
            if (!p.bins[i].isInstantiated()) {
                DisposableValueIterator it = p.bins[i].getValueIterator(true);
                try {
                    while (it.hasNext()) {
                        candidate.get(it.next()).set(i);
                    }
                } finally {
                    it.dispose();
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
    private void filterFullDim(int bin, int dim) throws ContradictionException {
        for (int i = candidate.get(bin).nextSetBit(0); i >= 0; i = candidate.get(bin).nextSetBit(i + 1)) {
            // ISSUE 86: the event 'i removed from bin' can already been in the propagation stack but not yet considered
            // ie. !p.bins[i].contains(bin) && candidate[bin].contains(i): in this case, do not process it yet
            if (p.bins[i].contains(bin) && p.iSizes[dim][i] > 0) {
                p.bins[i].removeValue(bin, p);
                candidate.get(bin).clear(i);
                p.potentialLoad[dim][bin].add(-p.iSizes[dim][i]);
                if (p.bins[i].isInstantiated()) {
                    p.assignItem(i, p.bins[i].getValue());
                }
            }
        }
        if (candidate.get(bin).isEmpty()) {
            assert p.potentialLoad[dim][bin].get() == p.assignedLoad[dim][bin].get();
            assert p.loads[dim][bin].getUB() == p.potentialLoad[dim][bin].get();
        }
    }

    /**
     * update the candidate list of a bin when an item is removed
     *
     * @param item the removed item
     * @param bin  the bin
     */
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
    protected void postAssignItem(int item, int bin) throws ContradictionException {
        if (candidate.get(bin).get(item)) { //TODO stop the recursive loop without this (see test2DWithUnorderedItems(seed=120))
            candidate.get(bin).clear(item);
            for (int d = 0; d < p.nbDims; d++) {
                if (p.assignedLoad[d][bin].get() == p.loads[d][bin].getUB()) {
                    filterFullDim(bin, d);
                    if (candidate.get(bin).isEmpty()) {
                        for (int d2 = 0; d2 < p.nbDims; d2++) {
                            p.potentialLoad[d2][bin].set(p.assignedLoad[d2][bin].get());
                            p.filterLoadSup(d2, bin, p.potentialLoad[d2][bin].get());
                        }
                        return;
                    }
                }
            }

        }
    }
}
