/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.solver.choco.extensions;


import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import memory.IStateIntVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.BitSet;

/**
 * A constraint to schedule tasks with regards to their resource usages on resources having a finite amount to share.
 * Tasks and resources can have multiple dimensions.
 * There is only 2 kind of tasks. cTasks that are already placed and necessarily starts at 0 and dTasks that
 * are not placed but end necessarily at the end of the schedule.
 * Inspired by the cumulative constraint.
 *
 * @author Fabien Hermenier
 */
public class TaskScheduler extends IntConstraint<IntVar> {

    private static final Logger LOGGER = LoggerFactory.getLogger("solver");

    private LocalTaskScheduler[] scheds;

    private IntVar[] cHosters;

    private IntVar[] cEnds;

    private IntVar[] dHosters;

    private IntVar[] dStarts;

    private int nbResources;

    private int nbDims;

    private int[][] capacities;

    private int[][] cUsages;

    private int[][] dUsages;

    private IStateIntVector[] vIns;

    /**
     * Make a new constraint.
     *
     * @param earlyStarts a variable for each resource to indicate the earliest moment a task can arrive on the resource
     * @param lastEnds    a variable for each resource to indicate the latest moment a task can stay on the resource
     * @param capas       for each dimension, the capacity of each resource
     * @param cHosters    the placement variable of each cTask
     * @param cUsages     the resource usage of each cTask for each dimension
     * @param cEnds       the moment each cTask ends
     * @param dHosters    the placement variable of each dTask
     * @param dUsages     the resource usage of each dTask for each dimension
     * @param dStarts     the moment each dTask starts
     * @param assocs      indicate association between cTasks and dTasks. Associated tasks cannot overlap on a same resource
     */
    public TaskScheduler(IntVar[] earlyStarts,
                         IntVar[] lastEnds,
                         int[][] capas,
                         IntVar[] cHosters,
                         int[][] cUsages,
                         IntVar[] cEnds,
                         IntVar[] dHosters,
                         int[][] dUsages,
                         IntVar[] dStarts,
                         int[] assocs,
                         Solver s) {

        super(ArrayUtils.append(dHosters, cHosters, cEnds, dStarts, earlyStarts, lastEnds), s);

        this.cHosters = cHosters;
        this.dHosters = dHosters;
        this.cEnds = cEnds;
        this.dStarts = dStarts;

        this.capacities = capas;
        this.cUsages = cUsages;
        this.dUsages = dUsages;

        this.nbResources = capas[0].length;
        this.nbDims = capas.length;

        scheds = new LocalTaskScheduler[nbResources];


        this.vIns = new IStateIntVector[scheds.length];
        setPropagators(new TaskSchedulerPropagator(earlyStarts, lastEnds, capas, cHosters, cUsages, cEnds, dHosters, dUsages, dStarts, assocs));
    }

    @Override
    public ESat isSatisfied(int[] vals) {
        //Split this use tab to ease the analysis
        int[] dHostersVals = new int[dHosters.length];
        int[] dStartsVals = new int[dStarts.length];
        int[] cHostersVals = new int[cHosters.length];
        int[] cEndsVals = new int[cEnds.length];

        //dHosters, cHosters, cEnds, dStarts
        for (int i = 0; i < dHosters.length; i++) {
            dHostersVals[i] = vals[i];
            dStartsVals[i] = vals[i + dHosters.length + cHosters.length + cEnds.length];
        }

        for (int i = 0; i < cHosters.length; i++) {
            cHostersVals[i] = vals[i + dHosters.length];
            cEndsVals[i] = vals[i + dHosters.length + cHosters.length];
        }

        //A hashmap to save the changes of each node (relatives to the previous moment) and each dimension
        TIntIntHashMap[][] changes = new TIntIntHashMap[nbDims][nbResources];

        for (int d = 0; d < nbDims; d++) {
            for (int j = 0; j < nbResources; j++) {
                changes[d][j] = new TIntIntHashMap();
            }
        }


        for (int d = 0; d < nbDims; d++) {
            for (int j = 0; j < dHostersVals.length; j++) {
                //for each placed dSlices, we get the used resource
                int r = dHostersVals[j];
                int st = dStartsVals[j];
                changes[d][r].put(st, changes[d][r].get(st) - dUsages[d][j]);
            }
        }

        int[][] currentFree = new int[nbDims][];
        for (int i = 0; i < nbDims; i++) {
            currentFree[i] = Arrays.copyOf(capacities[i], capacities[i].length);
        }

        for (int d = 0; d < nbDims; d++) {
            for (int j = 0; j < cHostersVals.length; j++) {
                int r = cHostersVals[j];
                int h = cUsages[d][j];
                int ed = cEndsVals[j];
                changes[d][r].put(ed, changes[d][r].get(ed) + h);
                currentFree[d][r] -= h;
            }
        }

        boolean ok = true;
        for (int j = 0; j < nbResources; j++) {
            TIntObjectHashMap<int[]> myChanges = myChanges(changes, j);
            LOGGER.debug("--- Resource " + j + " isSatisfied() ? ---");
            LOGGER.debug(" before: " + prettyUsages(currentFree, j)
                    + "/" + prettyUsages(capacities, j)
                    + " changes: " + prettyChanges(myChanges));


            int[] moments = myChanges.keys(new int[myChanges.size()]);
            Arrays.sort(moments);
            for (int t : moments) {
                boolean bad = true;
                for (int d = 0; d < nbDims; d++) {
                    currentFree[d][j] += myChanges.get(t)[d];
                    if (currentFree[d][j] < 0) {
                        bad = false;
                    }
                }
                if (!bad) {
                    LOGGER.info("/!\\ at " + t + ": free=" + prettyUsages(currentFree, j));
                    ok = false;
                    break;
                }
            }

            if (LOGGER.isDebugEnabled()) {
                for (int x = 0; x < cHostersVals.length; x++) {
                    if (cHostersVals[x] == j) {
                        LOGGER.debug(cEnds[x].getName() + " ends at " + cEndsVals[x] + " uses:" + prettyUsages(cUsages, x));
                    }
                }
                for (int x = 0; x < dHostersVals.length; x++) {
                    if (dHostersVals[x] == j) {
                        LOGGER.debug(dStarts[x].getName() + " starts at " + dStartsVals[x] + " uses:" + prettyUsages(dUsages, x));
                    }
                }
            }
        }
        return ESat.eval(ok);
    }

    private String prettyUsages(int[][] usages, int i) {
        int[] u = new int[nbDims];
        for (int x = 0; x < nbDims; x++) {
            u[x] = usages[x][i];
        }
        return Arrays.toString(u);
    }

    private TIntObjectHashMap<int[]> myChanges(TIntIntHashMap[][] changes, int nIdx) {
        TIntObjectHashMap<int[]> map = new TIntObjectHashMap<>();
        for (int d = 0; d < changes.length; d++) {
            TIntIntHashMap ch = changes[d][nIdx];
            for (int k : ch.keys()) {
                int[] upd = map.get(k);
                if (upd == null) {
                    upd = new int[changes.length];
                    map.put(k, upd);
                }
                upd[d] += changes[d][nIdx].get(k);
            }
        }
        return map;
    }

    private String prettyChanges(TIntObjectHashMap<int[]> changes) {
        int[] moments = changes.keys(new int[changes.size()]);
        Arrays.sort(moments);
        StringBuilder b = new StringBuilder();
        for (int t : moments) {
            b.append(t).append('=').append(Arrays.toString(changes.get(t))).append(' ');
        }
        return b.toString();
    }

    class TaskSchedulerPropagator extends Propagator<IntVar> {

        private IntVar[] earlyStarts, lastEnds;

        public TaskSchedulerPropagator(IntVar[] earlyStarts,
                                       IntVar[] lastEnds,
                                       int[][] capas,
                                       IntVar[] cHosters,
                                       int[][] cUsages,
                                       IntVar[] cEnds,
                                       IntVar[] dHosters,
                                       int[][] dUsages,
                                       IntVar[] dStarts,
                                       int[] assocs) {
            super(ArrayUtils.append(dHosters, cHosters, cEnds, dStarts, earlyStarts, lastEnds), PropagatorPriority.VERY_SLOW, true);

            this.earlyStarts = earlyStarts;
            this.lastEnds = lastEnds;
            BitSet[] outs = new BitSet[scheds.length];
            for (int i = 0; i < scheds.length; i++) {
                outs[i] = new BitSet(cHosters.length);
            }

            for (int i = 0; i < cHosters.length; i++) {
                outs[cHosters[i].getValue()].set(i);
            }

            int nbCTasks = cUsages[0].length;

            int[] revAssociations = new int[nbCTasks];
            for (int i = 0; i < revAssociations.length; i++) {
                revAssociations[i] = LocalTaskScheduler.NO_ASSOCIATIONS;
            }

            for (int i = 0; i < assocs.length; i++) {
                if (assocs[i] != LocalTaskScheduler.NO_ASSOCIATIONS) {
                    revAssociations[assocs[i]] = i;
                }
            }

            for (int i = 0; i < scheds.length; i++) {
                vIns[i] = earlyStarts[0].getSolver().getEnvironment().makeIntVector(0, 0);
                scheds[i] = new LocalTaskScheduler(i,
                        earlyStarts[i],
                        lastEnds[i],
                        capacities,
                        cUsages,
                        cEnds,
                        outs[i],
                        dUsages,
                        dStarts,
                        vIns[i],
                        assocs,
                        revAssociations,
                        aCause
                );
            }
        }


        @Override
        protected int getPropagationConditions(int vIdx) {
            return EventType.INSTANTIATE.mask;
        }

        private boolean first = true;

        @Override
        public void propagate(int idx, int mask) throws ContradictionException {

            if (idx < dHosters.length) {
                int nIdx = vars[idx].getValue();
                vIns[nIdx].add(idx);
            }
            forcePropagate(EventType.INSTANTIATE);
        }

        @Override
        public ESat isEntailed() {
            //Split this use tab to ease the analysis
            int[] dHostersVals = new int[dHosters.length];
            int[] dStartsVals = new int[dStarts.length];
            int[] cHostersVals = new int[cHosters.length];
            int[] cEndsVals = new int[cEnds.length];

            //dHosters, cHosters, cEnds, dStarts
            for (int i = 0; i < dHosters.length; i++) {
                dHostersVals[i] = dHosters[i].getValue();
                dStartsVals[i] = dStarts[i].getValue();
                if (dStartsVals[i] < earlyStarts[dHostersVals[i]].getValue()) {
                    LOGGER.error("D-slice " + dHosters[i] + " arrives too early: " + dStartsVals[i] + ". Min expected: " + earlyStarts[dHosters[i].getValue()]);
                    return ESat.FALSE;
                }
            }

            for (int i = 0; i < cHosters.length; i++) {
                cHostersVals[i] = cHosters[i].getValue();
                cEndsVals[i] = cEnds[i].getValue();
                if (cEndsVals[i] > lastEnds[cHostersVals[i]].getValue()) {
                    LOGGER.error("C-slice " + cHosters[i] + " leaves too late: " + cEndsVals[i] + ". Max expected: " + lastEnds[cHosters[i].getValue()]);
                    return ESat.FALSE;
                }

            }

            //A hashmap to save the changes of each node (relatives to the previous moment) and each dimension
            TIntIntHashMap[][] changes = new TIntIntHashMap[nbDims][nbResources];

            for (int d = 0; d < nbDims; d++) {
                for (int j = 0; j < nbResources; j++) {
                    changes[d][j] = new TIntIntHashMap();
                }
            }


            for (int d = 0; d < nbDims; d++) {
                for (int j = 0; j < dHostersVals.length; j++) {
                    //for each placed dSlices, we get the used resource
                    int r = dHostersVals[j];
                    int st = dStartsVals[j];
                    changes[d][r].put(st, changes[d][r].get(st) - dUsages[d][j]);
                }
            }

            int[][] currentFree = new int[nbDims][];
            for (int i = 0; i < nbDims; i++) {
                currentFree[i] = Arrays.copyOf(capacities[i], capacities[i].length);
            }

            for (int d = 0; d < nbDims; d++) {
                for (int j = 0; j < cHostersVals.length; j++) {
                    int r = cHostersVals[j];
                    int h = cUsages[d][j];
                    int ed = cEndsVals[j];
                    changes[d][r].put(ed, changes[d][r].get(ed) + h);
                    currentFree[d][r] -= h;
                }
            }

            boolean ok = true;
            for (int j = 0; j < nbResources; j++) {
                TIntObjectHashMap<int[]> myChanges = myChanges(changes, j);
                LOGGER.debug("--- Resource " + j + " isSatisfied() ? ---");
                LOGGER.debug(" before: " + prettyUsages(currentFree, j)
                        + "/" + prettyUsages(capacities, j)
                        + " changes: " + prettyChanges(myChanges));


                int[] moments = myChanges.keys(new int[myChanges.size()]);
                Arrays.sort(moments);
                for (int t : moments) {
                    boolean bad = true;
                    for (int d = 0; d < nbDims; d++) {
                        currentFree[d][j] += myChanges.get(t)[d];
                        if (currentFree[d][j] < 0) {
                            bad = false;
                        }
                    }
                    if (!bad) {
                        LOGGER.info("/!\\ at " + t + ": free=" + prettyUsages(currentFree, j));
                        ok = false;
                        break;
                    }
                }

                if (LOGGER.isDebugEnabled()) {
                    for (int x = 0; x < cHostersVals.length; x++) {
                        if (cHostersVals[x] == j) {
                            LOGGER.debug(cEnds[x].getName() + " ends at " + cEndsVals[x] + " uses:" + prettyUsages(cUsages, x));
                        }
                    }
                    for (int x = 0; x < dHostersVals.length; x++) {
                        if (dHostersVals[x] == j) {
                            LOGGER.debug(dStarts[x].getName() + " starts at " + dStartsVals[x] + " uses:" + prettyUsages(dUsages, x));
                        }
                    }
                }
            }
            return ESat.eval(ok);
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            if (first) {
                first = false;
                boolean isFull = true;
                for (int i = 0; i < dHosters.length; i++) {
                    if (dHosters[i].instantiated()) {
                        int nIdx = dHosters[i].getValue();
                        vIns[nIdx].add(i);
                    } else {
                        isFull = false;
                    }
                }
                //Already completely instantiated, need to propagate
                if (isFull) {
                    for (int j = 0; j < scheds.length; j++) {
                        if (!scheds[j].propagate()) {
                            this.contradiction(earlyStarts[j], "Invalid profile on resource '" + j + "'");
                        }
                    }
                }
            } else {
                long size;
                do {
                    size = 0;
                    for (IntVar v : vars) {
                        size += v.getDomainSize();
                    }
                    boolean isFull = true;
                    for (IntVar v : dHosters) {
                        if (!v.instantiated()) {
                            isFull = false;
                            break;
                        }
                    }
                    if (isFull) {
                        for (int i = 0; i < scheds.length; i++) {
                            if (!scheds[i].propagate()) {
                                this.contradiction(earlyStarts[i], "Invalid profile on resource '" + i + "'");
                            }
                        }
                    }
                    for (IntVar v : vars) {
                        size -= v.getDomainSize();
                    }
                } while (size > 0);
            }
        }
    }
}
