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

package btrplace.solver.choco.extensions;


import memory.IStateBool;
import memory.IStateInt;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.delta.IIntDeltaMonitor;
import util.ESat;
import util.iterators.DisposableValueIterator;
import util.procedure.UnaryIntProcedure;
import util.tools.ArrayUtils;

import java.util.*;

/**
 * Lighter but faster version of a bin packing that does not provide the knapsack filtering
 * propagate 1) globally: sumItemSizes == sumBinLoads 2) on each bin: sumAssignedItemSizes == binLoad
 * rule 1.0: if sumSizes < sumBinLoadInf or sumSizes > sumBinLoadSups then fail
 * rule 1.1, for each bin: sumItemSizes - sumOtherBinLoadSups <= binLoad <= sumItemSizes - sumOtherBinLoadInf
 * rule 2.0, for each bin: binRequiredLoad <= binLoad <= binTotalLoad
 *
 * @author Fabien Hermenier
 */
public class LightBinPackingPropagator extends Propagator<IntVar> {

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
     * The load of each bin per dimension. [nbDims][nbBins]
     */
    private final IntVar[][] loads;

    /**
     * The sum of the item sizes per dimension. [nbItems]
     */
    private long[] sumISizes;
    /**
     * The total size of the required + candidate items for each bin. [nbDims][nbBins]
     */
    private IStateInt[][] loadSup;

    /**
     * The total size of the required items for each bin. [nbDims][nbBins]
     */
    private IStateInt[][] loadInf;

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

//    private IStateBitSet notEntailedDims;


	protected final RemProc remProc;
	protected final IIntDeltaMonitor[] deltaMonitor;

    private ArrayList<PriorityQueue<Integer>> heap;

    public LightBinPackingPropagator(String[] labels, IntVar[][] l, int[][] s, IntVar[] b) {
        this(labels, l, s, b, true);
    }

        /**
         * constructor of the FastBinPacking global constraint
         *
         * @param labels the label describing each dimension
         * @param l      array of nbBins variables, each figuring the total size of the items assigned to it, usually initialized to [0, capacity]
         * @param s      array of nbItems, each figuring the item size.
         * @param b      array of nbItems variables, each figuring the possible bins an item can be assigned to, usually initialized to [0, nbBins-1]
         */
    public LightBinPackingPropagator(String[] labels, IntVar[][] l, int[][] s, IntVar[] b, boolean withHeap) {
        super(ArrayUtils.append(b, ArrayUtils.flatten(l)), PropagatorPriority.VERY_SLOW, true);
        this.name = labels;
        this.loads = l;
        this.nbBins = l[0].length;
        this.nbDims = l.length;
        this.bins = b;
        this.iSizes = s;
        this.loadSup = new IStateInt[nbDims][nbBins];
        this.loadInf = new IStateInt[nbDims][nbBins];
        this.remProc = new RemProc(this);
		this.deltaMonitor = new IIntDeltaMonitor[b.length];
        for (int i = 0; i < deltaMonitor.length; i++) {
            deltaMonitor[i] = this.vars[i].monitorDelta(this);
        }
        this.heap = null;
        if (withHeap) {
            this.heap = new ArrayList<>(nbDims);
            for (int d=0; d<nbDims; d++) {
                heap.add(new PriorityQueue<>(nbBins, new DeltaLoadComparator(d, this)));
            }
        }
    }

    public int deltaLoad(int dim, int bin) {
        return loads[dim][bin].getUB() - loads[dim][bin].getLB();
    }

    static class DeltaLoadComparator implements Comparator<Integer> {
        private int dimension;
        private LightBinPackingPropagator p;
        public DeltaLoadComparator(int dim, LightBinPackingPropagator p) {
            this.dimension = dim;
            this.p = p;
        }

        public int compare(Integer a, Integer b) {
            return p.deltaLoad(dimension, b) - p.deltaLoad(dimension, a);
        }
    }

    public ESat isConsistent() {
        int[][] l = new int[nbDims][nbBins];
        for (int i = 0; i < bins.length; i++) {
            if (bins[i].instantiated()) {
                for (int d = 0; d < nbDims; d++) {
                    int v = bins[i].getValue();
                    l[d][v] += iSizes[d][i];
                    if (l[d][v] > loads[d][v].getUB()) {
                        return ESat.FALSE;
                    }
                }
            }
        }
        return ESat.TRUE;
    }

    //***********************************************************************************************************************//
	// EVENTS
    //***********************************************************************************************************************//

	/**
	 * react on removal events on bins[] variables
	 * react on bound events on loads[] variables
	 */
    @Override
    public int getPropagationConditions(int idx) {
		return (idx < bins.length ? EventType.INT_ALL_MASK() : EventType.BOUND.mask + EventType.INSTANTIATE.mask);
    }

    @Override
    public ESat isEntailed() {
        return (isCompletelyInstantiated()) ? isConsistent() : ESat.UNDEFINED;
    }


    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & EventType.FULL_PROPAGATION.mask) != 0) {
            initialize();
        } else if (loadsHaveChanged.get()) {
			recomputeLoadSums();
        }
        if (heap != null) fixPointWithHeap();
        else fixPointWithoutHeap();
        assert checkLoadConsistency();
    }


    public void fixPointWithoutHeap() throws ContradictionException {
        boolean noFixPoint = true;
        while (noFixPoint) {
            for (int d=0; d<nbDims; d++) {
                if (sumISizes[d] > sumLoadSup[d].get() || sumISizes[d] < sumLoadInf[d].get()) {
                    contradiction(null, "");
                }
            }
            noFixPoint = false;

            for (int d=0; d<nbDims; d++) {
                for (int b=0; b<nbBins; b++) {
					assert(loads[d][b].getLB() >= loadInf[d][b].get() && loads[d][b].getUB() <= loadSup[d][b].get());
					noFixPoint |= filterLoadInf(d, b, (int) sumISizes[d] - sumLoadSup[d].get() + loads[d][b].getUB());
					noFixPoint |= filterLoadSup(d, b, (int) sumISizes[d] - sumLoadInf[d].get() + loads[d][b].getLB());
				}
            }
        }
    }

    public void fixPointWithHeap() throws ContradictionException {
        for (int d=0; d<nbDims; d++) {
            if (sumISizes[d] > sumLoadSup[d].get() || sumISizes[d] < sumLoadInf[d].get()) {
                contradiction(null, "");
            }
        }
        for (int d=0; d<nbDims; d++) {
            int nChanges;
            long deltaFromInf = sumISizes[d] - sumLoadInf[d].get();
            long deltaToSup = sumLoadSup[d].get() - sumISizes[d];
            do {
                nChanges = 0;
                if (deltaToSup > deltaFromInf) {
                    nChanges += filterLoads(d, (int)deltaFromInf, true);
                    deltaToSup = sumLoadSup[d].get() - sumISizes[d];
                    if (deltaToSup < 0) contradiction(null, "");
                    nChanges += filterLoads(d, (int)deltaToSup, false);
                    deltaFromInf = sumISizes[d] - sumLoadInf[d].get();
                    if (deltaFromInf < 0) contradiction(null, "");

                } else {
                    nChanges += filterLoads(d, (int)deltaToSup, false);
                    deltaFromInf = sumISizes[d] - sumLoadInf[d].get();
                    if (deltaFromInf < 0) contradiction(null, "");
                    nChanges += filterLoads(d, (int)deltaFromInf, true);
                    deltaToSup = sumLoadSup[d].get() - sumISizes[d];
                    if (deltaToSup < 0) contradiction(null, "");

                }
            } while (nChanges > 0);
        }
    }


    private int filterLoads(int d, int delta, boolean isSup) throws ContradictionException {
        assert heap != null;
        int nChanges = 0;
        if (deltaLoad(d, heap.get(d).peek()) > delta) {
            do {
                int b = heap.get(d).poll();
                if (isSup) filterLoadSup(d, b, delta + loads[d][b].getLB());
                else filterLoadInf(d, b, loads[d][b].getUB() - delta);
                assert(deltaLoad(d, b) == delta);
                heap.get(d).offer(b);
                nChanges++;
            } while (!heap.get(d).isEmpty() && deltaLoad(d, heap.get(d).peek()) > delta);
        }
        return nChanges;
    }


    @Override
		public void propagate(int idx, int mask) throws ContradictionException {
        if (idx < bins.length) {
            deltaMonitor[idx].freeze();
            deltaMonitor[idx].forEach(remProc.set(idx), EventType.REMOVE);
            deltaMonitor[idx].unfreeze();
			if (vars[idx].instantiated()) {
				assignItem(idx, vars[idx].getValue());
			}
        } else {
			loadsHaveChanged.set(true);
		}
        forcePropagate(EventType.CUSTOM_PROPAGATION);
    }
	
    /**
     * when an item is removed from a bin: update the candidate list and load of the bin
     * possibly update the UB of load[bin] to loadSup[bin] then synchronize sumLoadSup
     * @throws solver.exception.ContradictionException on the load[bin] variable
     */
    protected void removeItem(int item, int bin) throws ContradictionException {
        for (int d=0; d<nbDims; d++) {
            filterLoadSup(d, bin, loadSup[d][bin].add(-1 * iSizes[d][item]));
		}
	}
	
    /**
     * when an item is assigned to a bin: update the candidate list and the required load of the bin // TODO
     * possibly update the LB of load[bin] to loadInf[bin] then synchronize sumLoadInf
     * @throws solver.exception.ContradictionException on the load[bin] variable
     */
    private void assignItem(int item, int bin) throws ContradictionException {
        for (int d=0; d<nbDims; d++) {
            filterLoadInf(d, bin, loadInf[d][bin].add(iSizes[d][item]));
		}
    }

    private boolean filterLoadInf(int dim, int bin, int newLoadInf) throws ContradictionException {
        int delta = newLoadInf - loads[dim][bin].getLB();
        if (delta <= 0) 
			return false;
		loads[dim][bin].updateLowerBound(newLoadInf, aCause);
		if (sumISizes[dim] < sumLoadInf[dim].add(delta))
			contradiction(null, "");
		return true;
    }

    private boolean filterLoadSup(int dim, int bin, int newLoadSup) throws ContradictionException {
        int delta = newLoadSup - loads[dim][bin].getUB();
        if (delta >= 0) 
			return false;
		loads[dim][bin].updateUpperBound(newLoadSup, aCause);
		if (sumISizes[dim] > sumLoadSup[dim].add(delta)) 
			contradiction(null, "");
		return true;
    }

	private static class RemProc implements UnaryIntProcedure<Integer> {

        private final LightBinPackingPropagator p;
        private int idxVar;

        public RemProc(LightBinPackingPropagator p) {
            this.p = p;
        }

        @Override
        public UnaryIntProcedure set(Integer idxVar) {
            this.idxVar = idxVar;
            return this;
        }

        @Override
        public void execute(int val) throws ContradictionException {
			p.removeItem(idxVar, val);
        }
    }



    //***********************************************************************************************************************//
	// HELPER
    //***********************************************************************************************************************//


    /**
     * initialize the internal data: availableBins, candidates, binRequiredLoads, binTotalLoads, sumLoadInf, sumLoadSup
     * shrink the item-to-bins assignment variables: 0 <= bins[i] <= nbBins
     * shrink the bin load variables: binRequiredLoad <= binLoad <= binTotalLoad
     */
    public void initialize() throws ContradictionException {

        sumISizes = new long[nbDims];
//        notEntailedDims = environment.makeBitSet(nbDims);
//        notEntailedDims.clear(0, 3);

        computeSums();

        int[][] rLoads = new int[nbDims][nbBins];
        int[][] cLoads = new int[nbDims][nbBins];

        int[] sumFreeSize = new int[nbDims];
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
                    sumFreeSize[d] += iSizes[d][i];
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
                loadInf[d][b] = environment.makeInt(rLoads[d][b]);
                loadSup[d][b] = environment.makeInt(rLoads[d][b] + cLoads[d][b]);
                loads[d][b].updateLowerBound(rLoads[d][b], aCause);
                loads[d][b].updateUpperBound(rLoads[d][b] + cLoads[d][b], aCause);
                slb[d] += loads[d][b].getLB();
                slu[d] += loads[d][b].getUB();
                if (heap != null) heap.get(d).offer(b);
            }
        }

        sumLoadInf = new IStateInt[nbDims];
        sumLoadSup = new IStateInt[nbDims];
        for (int d = 0; d < nbDims; d++) {
            this.sumLoadInf[d] = environment.makeInt(slb[d]);
            this.sumLoadSup[d] = environment.makeInt(slu[d]);
        }

        this.loadsHaveChanged = environment.makeBool(false);

//        detectEntailedDimensions(sumFreeSize);

        assert checkLoadConsistency();
 //       LOGGER.trace("BinPacking: " + Arrays.toString(name) + " notEntailed dimensions: " + notEntailedDims);
        LOGGER.trace("BinPacking: " + Arrays.toString(name));

    }

    /**
     * Detect entailed dimensions
     * A dimension is entailed if for every bin, the free load (diff between the UB and the LB) is
     * >= the un-assigned height for that dimension
     *
//     * @param sumFreeSize the un-assigned height for each dimension.
     *
    private void detectEntailedDimensions(int[] sumFreeSize) {
        for (int d = 0; d < nbDims; d++) {
            for (int b = 0; b < nbBins; b++) {
                if (!loads[d][b].instantiated() && loads[d][b].getUB() - loads[d][b].getLB() < sumFreeSize[d]) {
                    notEntailedDims.set(d);
                    break;
                }
            }
        }
    }
    */
    public void printHeapsAndEmpty() {
        for (int d = 0; d < nbDims; d++) {
            System.out.println("heap for dimension " + d);
            while (!heap.get(d).isEmpty()) {
                System.out.print(heap.get(d).poll());
            }
            System.out.println();
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

    /**
     * recompute the sum of the min/max loads only if at least one variable bound has been updated outside the constraint
     */
    private void recomputeLoadSums() {
        loadsHaveChanged.set(false);
        for (int d=0; d<nbDims; d++) {
            if (heap != null) heap.get(d).clear();
            int sli = 0;
            int sls = 0;
            for (int b = 0; b < nbBins; b++) {
                sli += loads[d][b].getLB();
                sls += loads[d][b].getUB();
                if (heap != null) heap.get(d).offer(b);
            }
            this.sumLoadInf[d].set(sli);
            this.sumLoadSup[d].set(sls);
        }
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

        for (int d = 0; d < nbDims; d++) {
            int sli = 0;
            int sls = 0;
            for (int b = 0; b < rs[d].length; b++) {
                if (rs[d][b] != loadInf[d][b].get()) {
                    LOGGER.warn(name[d] + ": " + loads[d][b].toString() + " required=" + loadInf[d][b].get() + " expected=" + Arrays.toString(rs[b]));
                    check = false;
                }
                if (rs[d][b] + cs[d][b] != loadSup[d][b].get()) {
                    LOGGER.warn(name[d] + ": " + loads[d][b].toString() + " total=" + loadSup[d][b].get() + " expected=" + (rs[d][b] + cs[d][b]));
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
                    LOGGER.error(name[d] + ": " + loads[d][b].toString() + " required=" + loadInf[d][b].get() + " (" + rs[d][b] + ") total=" + loadSup[d][b].get() + " (" + (rs[d][b] + cs[d][b]) + ")");
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
