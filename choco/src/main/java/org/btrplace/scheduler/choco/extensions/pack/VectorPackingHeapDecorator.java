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

import org.chocosolver.solver.exception.ContradictionException;

import java.util.ArrayList;
import java.util.Comparator;
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
    private VectorPackingPropagator p;
    /**
     * The list of bins as a maxSlackBinHeap for quick access to the bin with the maximum slack load. [nbDims]
     */
    private ArrayList<PriorityQueue<Integer>> maxSlackBinHeap;

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
    public int loadSlack(int dim, int bin) {
        return p.loads[dim][bin].getUB() - p.loads[dim][bin].getLB();
    }

    public void reHeap() {
        for (int d = 0; d < p.nbDims; d++) {
            maxSlackBinHeap.get(d).clear();
            for (int b = 0; b < p.nbBins; b++) {
                if (loadSlack(d, b) > 0) {
                    maxSlackBinHeap.get(d).offer(b);
                }
            }
        }
    }

    private void checkReHeap(boolean forceReHeap) throws ContradictionException {
        int currentWorld = p.getSolver().getEnvironment().getWorldIndex();
        long currentBt = p.getSolver().getMeasures().getBackTrackCount();
        long currentRestart = p.getSolver().getMeasures().getRestartCount();
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
        private int dimension;
        private VectorPackingHeapDecorator hp;

        public LoadSlackComparator(int dim, VectorPackingHeapDecorator hp) {
            this.dimension = dim;
            this.hp = hp;
        }

        public int compare(Integer a, Integer b) {
            return hp.loadSlack(dimension, b) - hp.loadSlack(dimension, a);
        }
    }

    /**
     * the fix point procedure with heaps for propagation rule 1.1, on each dimension:
     * - check rule 1.0: if sumItemSizes < sumBinLoadInf or sumItemSizes > sumBinLoadSup then fail
     * - filter binLoad according to rule 1.1, for each bin:
     * if loadSlack > sumItemSizes - sumBinLoadInf then update sup(binLoad) = sumItemSizes - (sumBinLoadInf - inf(binLoad))
     * if loadSlack > sumBinLoadSup - sumItemSizes then update inf(binLoad) = sumItemSizes - (sumBinLoadSup - sup(binLoad))
     * check each rule against the bin with the maximum loadSlack and continue until it does not apply
     *
     * @throws solver.exception.ContradictionException if a contradiction (rules 1) is raised
     */
    public void fixPoint(boolean loadsHaveChanged) throws ContradictionException {
        for (int d = 0; d < p.nbDims; d++) {
            if (p.sumISizes[d] > p.sumLoadSup[d].get() || p.sumISizes[d] < p.sumLoadInf[d].get()) {
                p.contradiction(null, "");
            }
        }
        checkReHeap(loadsHaveChanged);
        for (int d = 0; d < p.nbDims; d++) {
            if (maxSlackBinHeap.get(d).isEmpty()) continue;
            int nChanges;
            long deltaFromInf = p.sumISizes[d] - p.sumLoadInf[d].get();
            long deltaToSup = p.sumLoadSup[d].get() - p.sumISizes[d];
            do {
                nChanges = 0;
                if (deltaToSup > deltaFromInf) {
                    nChanges += filterLoads(d, (int) deltaFromInf, true);
                    deltaToSup = p.sumLoadSup[d].get() - p.sumISizes[d];
                    if (deltaToSup < 0) p.contradiction(null, "");
                    nChanges += filterLoads(d, (int) deltaToSup, false);
                    deltaFromInf = p.sumISizes[d] - p.sumLoadInf[d].get();
                    if (deltaFromInf < 0) p.contradiction(null, "");

                } else {
                    nChanges += filterLoads(d, (int) deltaToSup, false);
                    deltaFromInf = p.sumISizes[d] - p.sumLoadInf[d].get();
                    if (deltaFromInf < 0) p.contradiction(null, "");
                    nChanges += filterLoads(d, (int) deltaFromInf, true);
                    deltaToSup = p.sumLoadSup[d].get() - p.sumISizes[d];
                    if (deltaToSup < 0) p.contradiction(null, "");

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
    private int filterLoads(int d, int delta, boolean isSup) throws ContradictionException {
        assert maxSlackBinHeap != null;
        int nChanges = 0;
        if (maxSlackBinHeap == null || maxSlackBinHeap.get(d) == null || maxSlackBinHeap.get(d).isEmpty()) {
            System.out.println("coucou");
        }
        if (loadSlack(d, maxSlackBinHeap.get(d).peek()) > delta) {
            do {
                int b = maxSlackBinHeap.get(d).poll();
                if (isSup) p.filterLoadSup(d, b, delta + p.loads[d][b].getLB());
                else p.filterLoadInf(d, b, p.loads[d][b].getUB() - delta);
                assert (loadSlack(d, b) == delta);
                maxSlackBinHeap.get(d).offer(b);
                nChanges++;
            } while (!maxSlackBinHeap.get(d).isEmpty() && loadSlack(d, maxSlackBinHeap.get(d).peek()) > delta);
        }
        return nChanges;
    }


}
