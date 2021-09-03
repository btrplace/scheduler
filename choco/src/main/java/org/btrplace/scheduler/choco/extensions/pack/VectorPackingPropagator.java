/*
 * Copyright  2021 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.extensions.pack;


import org.chocosolver.memory.IStateBool;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.procedure.UnaryIntProcedure;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * Lighter but faster version of a multi dimension vector packing that does not provide the knapsack filtering
 * enforce:
 * 1) globally: sum(binLoads) == sumItemSizes
 * 2) on each bin: binLoad = sumAssignedItemSizes
 *
 * @author Sophie Demassey, Fabien Hermenier
 */
public class VectorPackingPropagator extends Propagator<IntVar> {

    /**
     * The number of bins.
     */
    protected final int nbBins;
    /**
     * The number of dimensions.
     */
    protected final int nbDims;
    /**
     * The bin assigned to each item. [nbItems]
     */
    protected final IntVar[] bins;
    /**
     * The constant size of each item. [nbDims][nbItems]
     */
    protected final int[][] iSizes;

    /**
     * The load of each bin per dimension. [nbDims][nbBins]
     */
    protected final IntVar[][] loads;
    /**
     * The procedure for removals in bins variable domains.
     */
    protected final RemProc remProc;
    /**
     * The list of removals in bins variable domains.
     */
    protected final IIntDeltaMonitor[] deltaMonitor;
    /**
     * The sum of the item sizes per dimension. [nbDims]
     */
    protected long[] sumISizes;
    /**
     * The total size of the assigned + candidate items for each bin. [nbDims][nbBins]
     */
    protected IStateInt[][] potentialLoad;
    /**
     * The total size of the assigned items for each bin. [nbDims][nbBins]
     */
    protected IStateInt[][] assignedLoad;
    /**
     * The sum of the bin load LBs. [nbDims]
     */
    protected IStateInt[] sumLoadInf;
    /**
     * The sum of the bin load UBs. [nbDims]
     */
    protected IStateInt[] sumLoadSup;
    /**
     * Constraint name.
     */
    protected String[] name;

  /**
   * Must the sumLoads be recompute since the last propagation ?
   */
  private IStateBool loadsHaveChanged;
  /**
   * The list of bins as a maxSlackBinHeap for quick access to the bin with the maximum slack load. [nbDims]
   */
  private final VectorPackingHeapDecorator decoHeap;
  private final KnapsackDecorator decoKPSimple;

  /**
   * constructor of the VectorPacking global constraint
   *
   * @param labels the label describing each dimension [nbDims]
   * @param l      array of nbDims x nbBins variables, each figuring the total size of the items assigned to it, usually initialized to [0, capacity]
   * @param s      array of nbDims x nbItems, each figuring the item size.
   * @param b      array of nbItems variables, each figuring the possible bins an item can be assigned to, usually initialized to [0, nbBins-1]
   */
  public VectorPackingPropagator(String[] labels, IntVar[][] l, int[][] s, IntVar[] b) {
        super(ArrayUtils.append(b, ArrayUtils.flatten(l)), PropagatorPriority.VERY_SLOW, true);
        this.name = labels;
        this.loads = l;
        this.nbBins = l[0].length;
        this.nbDims = l.length;
        this.bins = b;
        this.iSizes = s;
        this.remProc = new RemProc(this);
        this.deltaMonitor = new IIntDeltaMonitor[b.length];
        for (int i = 0; i < deltaMonitor.length; i++) {
            deltaMonitor[i] = this.vars[i].monitorDelta(this);
        }

        decoHeap = new VectorPackingHeapDecorator(this);
        decoKPSimple = new KnapsackDecorator(this);

        //make backtrackable stuff.
        this.potentialLoad = new IStateInt[nbDims][nbBins];
        this.assignedLoad = new IStateInt[nbDims][nbBins];
        for (int x = 0; x < nbBins; x++) {
            for (int d = 0; d < nbDims; d++) {
                assignedLoad[d][x] = getModel().getEnvironment().makeInt();
                potentialLoad[d][x] = getModel().getEnvironment().makeInt();
            }
        }
        sumLoadInf = new IStateInt[nbDims];
        sumLoadSup = new IStateInt[nbDims];
        for (int d = 0; d < nbDims; d++) {
            sumLoadInf[d] = getModel().getEnvironment().makeInt();
            sumLoadSup[d] = getModel().getEnvironment().makeInt();
        }
        super.linkVariables();
    }

    /**
     * check the consistency of the constraint.
     *
     * @return false if the total size of the items assigned to a bin exceeds the bin load upper bound, true otherwise
     */
    private ESat isConsistent() {
        int[][] l = new int[nbDims][nbBins];
        for (int i = 0; i < bins.length; i++) {
            if (bins[i].isInstantiated()) {
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
     * react on removal events on bins variables
     * react on bound events on loads variables
     */
    @Override
    public int getPropagationConditions(int idx) {
        return idx < bins.length ? IntEventType.all() : IntEventType.BOUND.getMask() + IntEventType.INSTANTIATE.getMask();
    }

    /**
     * TODO: check when no propagation may occur anymore
     *
     * @return if the consistency state of the constraint if is is instantiated, undefined otherwise
     */
    @Override
    public ESat isEntailed() {
        return isCompletelyInstantiated() ? isConsistent() : ESat.UNDEFINED;
    }

    /**
     * main propagation algorithm:
     * - initialize the data at first call
     * - recompute the sum of the load variable bounds
     * - propagate until the fix point (with or without considering the heap of bins)
     *
     * @param evtMask type of event
     * @throws ContradictionException if a contradiction is raised
     */
    @Override
    @SuppressWarnings("squid:S3346")
    public void propagate(int evtMask) throws ContradictionException {
        boolean recomputeLoads;
        if ((evtMask & PropagatorEventType.FULL_PROPAGATION.getMask()) != 0) {
            initialize();
            recomputeLoads = true;
        } else {
            recomputeLoads = loadsHaveChanged.get();
            if (recomputeLoads) {
                recomputeLoadSums(); // TODO: update rather than recompute
            }
        }
        if (decoHeap != null) {
            decoHeap.fixPoint(recomputeLoads);
        } else {
            fixPoint();
        }
        assert checkLoadConsistency();
    }


    /**
     * the fix point procedure without heap, on each dimension:
     * - check rule 1.0: if sumItemSizes < sumBinLoadInf or sumItemSizes > sumBinLoadSups then fail
     * - filter according to rule 1.1, for each bin: sumItemSizes - (sumBinLoadSup - sup(binLoad)) &lt;= binLoad <= sumItemSizes - (sumBinLoadInf - inf(binLoad))
     *
     * @throws ContradictionException if a contradiction (rules 1) is raised
     */
    @SuppressWarnings("squid:S3346")
    private void fixPoint() throws ContradictionException {
        boolean noFixPoint = true;
        while (noFixPoint) {
            for (int d = 0; d < nbDims; d++) {
                if (sumISizes[d] > sumLoadSup[d].get() || sumISizes[d] < sumLoadInf[d].get()) {
                    fails();
                }
            }
            noFixPoint = false;

            for (int d = 0; d < nbDims; d++) {
                for (int b = 0; b < nbBins; b++) {
                    assert (loads[d][b].getLB() >= assignedLoad[d][b].get() && loads[d][b].getUB() <= potentialLoad[d][b].get());
                    noFixPoint |= filterLoadInf(d, b, (int) sumISizes[d] - sumLoadSup[d].get() + loads[d][b].getUB());
                    noFixPoint |= filterLoadSup(d, b, (int) sumISizes[d] - sumLoadInf[d].get() + loads[d][b].getLB());
                }
            }
        }
    }

    /**
     * update the inf(binLoad) and sumLoadInf accordingly
     *
     * @param dim        the dimension
     * @param bin        the bin
     * @param newLoadInf the new lower bound value
     * @return if the lower bound has actually been updated
     * @throws ContradictionException if the domain of the bin load variable becomes empty
     */
    protected boolean filterLoadInf(int dim, int bin, int newLoadInf) throws ContradictionException {
        int delta = newLoadInf - loads[dim][bin].getLB();
        if (delta <= 0)
            return false;
        loads[dim][bin].updateLowerBound(newLoadInf, this);
        if (sumISizes[dim] < sumLoadInf[dim].add(delta))
            fails();
        return true;
    }

    /**
     * update sup(binLoad) and sumLoadSup accordingly
     *
     * @param dim        the dimension
     * @param bin        the bin
     * @param newLoadSup the new lower bound value
     * @return if the upper bound has actually been updated
     * @throws ContradictionException if the domain of the bin load variable becomes empty
     */
    protected boolean filterLoadSup(int dim, int bin, int newLoadSup) throws ContradictionException {
        int delta = newLoadSup - loads[dim][bin].getUB();
        if (delta >= 0) {
            return false;
        }
        loads[dim][bin].updateUpperBound(newLoadSup, this);
        if (sumISizes[dim] > sumLoadSup[dim].add(delta)) {
            fails();
        }
        return true;
    }


    /**
     * apply rule 2 (binLoad &lt;= binPotentialLoad) when an item has been removed from the bin candidate list
     *
     * @param item the item index
     * @param bin the bin index
     * @throws ContradictionException if a contradiction (rule 2) is raised
     */
    protected void removeItem(int item, int bin) throws ContradictionException {
        updateLoads(item, bin);
        if (decoKPSimple != null) {
            decoKPSimple.postRemoveItem(item, bin);
        }
    }

    protected void updateLoads(int item, int bin) throws ContradictionException {
        int d = 0;
        for (; d < nbDims; d++) {
            int s = iSizes[d][item];
            if (s > 0) {
                filterLoadSup(d, bin, potentialLoad[d][bin].add(-s));
            }
        }
    }

    /**
     * apply rule 2 (binLoad &gt;= binAssignedLoad) when an item has been assign to the bin
     *
     * @param item the item index
     * @param bin the bin index
     * @throws ContradictionException if a contradiction (rule 2) is raised
     */
    protected void assignItem(int item, int bin) throws ContradictionException {
        for (int d = 0; d < nbDims; d++) {
            filterLoadInf(d, bin, assignedLoad[d][bin].add(iSizes[d][item]));
        }
        if (decoKPSimple != null) {
            decoKPSimple.postAssignItem(item, bin);
        }
    }

    /**
     * fine grain propagation
     * - if the event concerns a bin variable, then update data and apply rule 2:
     * on the assigned bin: binAssignedLoad &lt;= binLoad &lt;= binPotentialLoad
     * - otherwise remember to recompute the load sums and do nothing
     *
     * @param idx  the variable index
     * @param mask the event mask
     * @throws ContradictionException if a contradiction (rule 2) is raised
     */
    @Override
    public void propagate(int idx, int mask) throws ContradictionException {
        if (idx < bins.length) {
            deltaMonitor[idx].freeze();
            deltaMonitor[idx].forEachRemVal(remProc.set(idx));
            deltaMonitor[idx].unfreeze();
            if (vars[idx].isInstantiated()) {
                assignItem(idx, vars[idx].getValue());
            }
        } else {
            loadsHaveChanged.set(true);
        }
        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }

    /**
     * initialize the internal data: sumItemSize, assignedLoad, potentialLoad, sumLoadInf, sumLoadSup, maxSlackBinHeap
     * shrink the item-to-bins assignment variables: 0 &lt;= bins[i] < nbBins
     * shrink the bin load variables: assignedLoad &lt;= binLoad &lt;= potentialLoad
     */
    @SuppressWarnings("squid:S3346")
    private void initialize() throws ContradictionException {

        sumISizes = new long[nbDims];
        computeSumItemSizes();

        int[][] rLoads = new int[nbDims][nbBins];
        int[][] cLoads = new int[nbDims][nbBins];

        // By default, the cLoad is the cumulative item size as by default, they may go to all the
        // bins.
        for (int d = 0; d < nbDims; d++) {
            for (int n = 0; n < nbBins; n++) {
                cLoads[d][n] = (int) sumISizes[d];
            }
        }
        for (int i = 0; i < bins.length; i++) {
            bins[i].updateLowerBound(0, this);
            bins[i].updateUpperBound(nbBins - 1, this);
            if (bins[i].isInstantiated()) {
              int bIdx = bins[i].getValue();
                for (int d = 0; d < nbDims; d++) {
                  rLoads[d][bIdx] += iSizes[d][i];
                }
                // Items placed are no longer candidate for any nodes.
                for (int n = 0; n < nbBins; n++) {
                    for (int d = 0; d < nbDims; d++) {
                        cLoads[d][n] -= iSizes[d][i];
                    }
                }
            } else {
                // We undeclare them candidate for the nodes where they can't go.
                if (bins[i].getDomainSize() == nbBins) {
                    // Fastpath.
                    continue;
                }
                for (int n = 0; n < nbBins; n++) {
                    if (!bins[i].contains(n)) {
                        for (int d = 0; d < nbDims; d++) {
                            cLoads[d][n] -= iSizes[d][i];
                        }
                    }
                }
            }
        }

        int[] slb = new int[nbDims];
        int[] slu = new int[nbDims];
        for (int b = 0; b < nbBins; b++) {
            for (int d = 0; d < nbDims; d++) {
                assignedLoad[d][b].set(rLoads[d][b]);
                potentialLoad[d][b].set(rLoads[d][b] + cLoads[d][b]);
                loads[d][b].updateLowerBound(rLoads[d][b], this);
                loads[d][b].updateUpperBound(rLoads[d][b] + cLoads[d][b], this);
                slb[d] += loads[d][b].getLB();
                slu[d] += loads[d][b].getUB();
            }
        }

        for (int d = 0; d < nbDims; d++) {
            sumLoadInf[d].set(slb[d]);
            sumLoadSup[d].set(slu[d]);
        }

        loadsHaveChanged = getModel().getEnvironment().makeBool(false);

        if (decoKPSimple != null) {
            decoKPSimple.postInitialize();
        }

        assert checkLoadConsistency();

        for (IIntDeltaMonitor delta : deltaMonitor) {
            delta.unfreeze();
        }
    }


    //***********************************************************************************************************************//
    // HELPER
    //***********************************************************************************************************************//

    /**
     * Compute the sum of the item sizes for each dimension.
     */
    private void computeSumItemSizes() {
        for (int d = 0; d < nbDims; d++) {
            long sum = 0;
            for (int i = 0; i < iSizes[d].length; i++) {
                sum += iSizes[d][i];
            }
            this.sumISizes[d] = sum;
        }

    }

    /**
     * recompute the sum of the min/max loads only if at least one variable bound has been updated outside of the constraint
     */
    private void recomputeLoadSums() {
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
    }

    /**
     * Check the consistency of the assigned and candidate loads with regards to the assignment variables:
     * for each bin: sumAssignedItemSizes == binAssignedLoad, sumAllPossibleItemSizes == binPotentialLoad
     * rule 2, for each bin: binAssignedLoad &lt;= binLoad &lt;= binPotentialLoad
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
            check = check && checkDimension(d, rs, cs);
        }
        if (!check) {
            for (IntVar v : bins) {
                System.out.println(v.toString());
            }
        }
        return check;
    }


    //****************************************************************//
    //********* Checkers *********************************************//
    //****************************************************************//

    @SuppressWarnings("squid:S106")
    private boolean checkDimension(int d, int[][] rs, int[][] cs) {
        boolean check = true;
        int sli = 0;
        int sls = 0;
        for (int b = 0; b < rs[d].length; b++) {
            if (rs[d][b] != assignedLoad[d][b].get()) {
                System.out.printf("%s: %s assigned=%d expected=%s%n", name[d], loads[d][b], assignedLoad[d][b].get(), Arrays.toString(rs[b]));
                check = false;
            }
            if (rs[d][b] + cs[d][b] != potentialLoad[d][b].get()) {
                System.out.printf("%s: %s potential=%d expected=%d (%d+%d)%n", name[d], loads[d][b], potentialLoad[d][b].get(), rs[d][b] + cs[d][b], rs[d][b], cs[d][b]);
                //System.out.println(name[d] + ": " + loads[d][b].toString() + " potential=" + potentialLoad[d][b].get() + " expected=" + (rs[d][b] + cs[d][b]) + ());
                check = false;
            }
            if (loads[d][b].getLB() < rs[d][b]) {
                System.out.printf("%s: %s LB expected >= %d%n", name[d], loads[d][b], rs[d][b]);
                check = false;
            }
            if (loads[d][b].getUB() > rs[d][b] + cs[d][b]) {
                System.out.printf("%s: %s UB expected <= %d%n", name[d], loads[d][b], rs[d][b] + cs[d][b]);
                check = false;
            }
            sli += loads[d][b].getLB();
            sls += loads[d][b].getUB();
        }
        if (this.sumLoadInf[d].get() != sli) {
            System.out.println(name[d] + ": " + "Sum Load LB = " + this.sumLoadInf[d].get() + " expected =" + sli);
            check = false;
        }
        if (this.sumLoadSup[d].get() != sls) {
            System.out.println(name[d] + ": " + "Sum Load UB = " + this.sumLoadSup[d].get() + " expected =" + sls);
            check = false;
        }
        return check;
    }

    public IStateInt[][] assignedLoad() {
        return assignedLoad;
    }

    /**
     * the procedure of removal for an assignment variable
     */
    private static class RemProc implements UnaryIntProcedure<Integer> {
        private final VectorPackingPropagator p;
        private int idxVar;

        public RemProc(VectorPackingPropagator p) {
            this.p = p;
        }

        @Override
        public UnaryIntProcedure<Integer> set(Integer idxVar) {
            this.idxVar = idxVar;
            return this;
        }

        @Override
        public void execute(int val) throws ContradictionException {
            p.removeItem(idxVar, val);
        }
    }
}
