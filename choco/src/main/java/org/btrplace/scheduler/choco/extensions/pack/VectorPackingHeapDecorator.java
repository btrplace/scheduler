/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions.pack;

import org.chocosolver.solver.exception.ContradictionException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * The list of bins can be optionally maintain as a heap in order that only the bins with the maximum load slack
 * are considered in the propagation of rule 1: binLoad = sumItemSizes - sumOtherBinLoads
 * call attachHeapDecorator() to the VectorPackingPropagator object
 *
 * @author Sophie Demassey
 */
public class VectorPackingHeapDecorator {

    /**
     * the core BinPacking propagator
     */
    private final VectorPackingPropagator p;
    /**
     * The list of bins as a maxSlackBinHeap for quick access to the bin with the maximum slack load. [nbDims]
     */
    private final List<PriorityQueue<Integer>> maxSlackBinHeap;

    private int lastWorld = -1;
    private long lastNbOfBacktracks = -1;
    private long lastNbOfRestarts = -1;

    public VectorPackingHeapDecorator(VectorPackingPropagator p) {
        this.p = p;
        this.maxSlackBinHeap = new ArrayList<>(p.nbDims);
        for (int d = 0; d < p.nbDims; d++) {
            maxSlackBinHeap.add(new PriorityQueue<>(p.nbBins, new LoadSlackComparator(d, this)));
        }
    }

    /**
     * compute the load slack of a bin
     *
     * @param dim the dimension
     * @param bin the bin
     * @return the load slack of bin on dimension bin
     */
    private int loadSlack(int dim, int bin) {
        return p.loads[dim][bin].getUB() - p.loads[dim][bin].getLB();
    }

    private void reHeap() {
        for (int d = 0; d < p.nbDims; d++) {
            maxSlackBinHeap.get(d).clear();
            for (int b = 0; b < p.nbBins; b++) {
                if (loadSlack(d, b) > 0) {
                    maxSlackBinHeap.get(d).offer(b);
                }
            }
        }
    }

    private void checkReHeap(boolean forceReHeap) {
        int currentWorld = p.getModel().getEnvironment().getWorldIndex();
        long currentBt = p.getModel().getSolver().getMeasures().getBackTrackCount();
        long currentRestart = p.getModel().getSolver().getMeasures().getRestartCount();
        if (forceReHeap || currentWorld < lastWorld || currentBt != lastNbOfBacktracks || currentRestart > lastNbOfRestarts) {
            reHeap();
        }
        lastWorld = currentWorld;
        lastNbOfBacktracks = currentBt;
        lastNbOfRestarts = currentRestart;
    }


    /**
     * a comparator of load slacks
     */
    static class LoadSlackComparator implements Comparator<Integer> {
        private final int dimension;
        private final VectorPackingHeapDecorator hp;

        public LoadSlackComparator(int dim, VectorPackingHeapDecorator hp) {
            this.dimension = dim;
            this.hp = hp;
        }

        @Override
        public int compare(Integer a, Integer b) {
            return hp.loadSlack(dimension, b) - hp.loadSlack(dimension, a);
        }
    }

    /**
     * the fix point procedure with heaps for propagation rule 1.1, on each dimension:
     * - check rule 1.0: if sumItemSizes &lt; sumBinLoadInf or sumItemSizes &gt; sumBinLoadSup then fail
     * - filter binLoad according to rule 1.1, for each bin:
     * if loadSlack &gt; sumItemSizes - sumBinLoadInf then update sup(binLoad) = sumItemSizes - (sumBinLoadInf - inf(binLoad))
     * if loadSlack &gt; sumBinLoadSup - sumItemSizes then update inf(binLoad) = sumItemSizes - (sumBinLoadSup - sup(binLoad))
     * check each rule against the bin with the maximum loadSlack and continue until it does not apply
     *
     * @param loadsHaveChanged {@code true} to indicate the load has changed. In that case, we reheap.
     * @throws ContradictionException if a contradiction (rules 1) is raised
     */
    public void fixPoint(boolean loadsHaveChanged) throws ContradictionException {
        for (int d = 0; d < p.nbDims; d++) {
            if (p.sumISizes[d] > p.sumLoadSup[d].get() || p.sumISizes[d] < p.sumLoadInf[d].get()) {
                p.fails();
            }
        }
        checkReHeap(loadsHaveChanged);
        for (int d = 0; d < p.nbDims; d++) {
            if (maxSlackBinHeap.get(d).isEmpty()) {
                continue;
            }
            int nChanges;
            long deltaFromInf = p.sumISizes[d] - p.sumLoadInf[d].get();
            long deltaToSup = p.sumLoadSup[d].get() - p.sumISizes[d];
            do {
                nChanges = 0;
                if (deltaToSup > deltaFromInf) {
                    nChanges += filterLoads(d, (int) deltaFromInf, true);
                    deltaToSup = p.sumLoadSup[d].get() - p.sumISizes[d];
                    if (deltaToSup < 0) {
                        p.fails();
                    }
                    nChanges += filterLoads(d, (int) deltaToSup, false);
                    deltaFromInf = p.sumISizes[d] - p.sumLoadInf[d].get();
                    if (deltaFromInf < 0) {
                        p.fails();
                    }

                } else {
                    nChanges += filterLoads(d, (int) deltaToSup, false);
                    deltaFromInf = p.sumISizes[d] - p.sumLoadInf[d].get();
                    if (deltaFromInf < 0) {
                        p.fails();
                    }
                    nChanges += filterLoads(d, (int) deltaFromInf, true);
                    deltaToSup = p.sumLoadSup[d].get() - p.sumISizes[d];
                    if (deltaToSup < 0) {
                        p.fails();
                    }

                }
            } while (nChanges > 0);
        }
    }


    /**
     * check each rule 1.1 (lower or upper bound) against the bin with the maximum loadSlack
     * possibly filter the bound of the binLoad variable, update the heap
     * and continue until the rule does not apply
     *
     * @param d     the dimension
     * @param delta the global slack to subtract from the bound
     * @param isSup is the bound the upper bound ?
     * @return the number of bound updates
     * @throws ContradictionException if a contradiction (rule 1.1) is raised
     */
    @SuppressWarnings("squid:S3346")
    private int filterLoads(int d, int delta, boolean isSup) throws ContradictionException {
        assert maxSlackBinHeap != null;
        int nChanges = 0;
        if (loadSlack(d, maxSlackBinHeap.get(d).peek()) > delta) {
            do {
                int b = maxSlackBinHeap.get(d).poll();
                if (isSup) {
                    p.filterLoadSup(d, b, delta + p.loads[d][b].getLB());
                } else {
                    p.filterLoadInf(d, b, p.loads[d][b].getUB() - delta);
                }
                assert loadSlack(d, b) == delta;
                maxSlackBinHeap.get(d).offer(b);
                nChanges++;
            } while (!maxSlackBinHeap.get(d).isEmpty() && loadSlack(d, maxSlackBinHeap.get(d).peek()) > delta);
        }
        return nChanges;
    }


}
