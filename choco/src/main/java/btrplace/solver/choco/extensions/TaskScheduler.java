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


import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import memory.IStateInt;
import memory.IStateIntVector;
import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
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
public class TaskScheduler extends Constraint {

    /**
     * Make a new constraint.
     *
     * @param earlyStarts a variable for each resource to indicate the earliest moment a task can arrive on the resource
     * @param lastEnds    a variable for each resource to indicate the latest moment a task can stay on the resource
     * @param capas       the capacity for each resource and for each dimension
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
                         int[] assocs) {

        super("TaskScheduler", new TaskSchedulerPropagator(earlyStarts, lastEnds, capas, cHosters, cUsages, cEnds, dHosters, dUsages, dStarts, assocs));
    }

    static class TaskSchedulerPropagator extends Propagator<IntVar> {

        private LocalTaskScheduler[] scheds;

        private IntVar[] cHosters;

        private IntVar[] cEnds;

        private IntVar[] dHosters;

        private IntVar[] dStarts;

        private int nbHosts;

        private int nbDims;

        private int[][] capacities;

        private int[][] cUsages;

        private int[][] dUsages;

        private IStateIntVector[] vIns;

        private IntVar[] earlyStarts, lastEnds;

        private IStateInt watchDTask;

        private BitSet watchHosts;

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
            super(ArrayUtils.append(dHosters, cHosters, cEnds, dStarts, earlyStarts, lastEnds), PropagatorPriority.VERY_SLOW, false);
            this.cHosters = cHosters;
            this.dHosters = dHosters;
            this.cEnds = cEnds;
            this.dStarts = dStarts;
            this.nbHosts = capas.length;
            this.nbDims = capas[0].length;

            assert cUsages.length == cHosters.length;
            assert dUsages.length == dHosters.length;
            assert cUsages.length == 0 || cUsages[0].length == nbDims;
            assert dUsages.length == 0 || dUsages[0].length == nbDims;
            this.capacities = capas;
            this.cUsages = cUsages;
            this.dUsages = dUsages;

            scheds = new LocalTaskScheduler[nbHosts];
            this.vIns = new IStateIntVector[nbHosts];

            this.earlyStarts = earlyStarts;
            this.lastEnds = lastEnds;
            BitSet[] outs = new BitSet[nbHosts];
            int nbCTasks = cHosters.length;
            for (int h = 0; h < nbHosts; h++) {
                outs[h] = new BitSet(nbCTasks);
            }

            for (int ct = 0; ct < nbCTasks; ct++) {
                assert cHosters[ct].isInstantiated();
                outs[cHosters[ct].getValue()].set(ct);
            }


            int[] revAssociations = new int[nbCTasks];
            for (int ct = 0; ct < revAssociations.length; ct++) {
                revAssociations[ct] = LocalTaskScheduler.NO_ASSOCIATIONS;
            }

            for (int dt = 0; dt < assocs.length; dt++) {
                if (assocs[dt] != LocalTaskScheduler.NO_ASSOCIATIONS) {
                    revAssociations[assocs[dt]] = dt;
                }
            }

            for (int h = 0; h < nbHosts; h++) {
                vIns[h] = earlyStarts[0].getSolver().getEnvironment().makeIntVector(0, 0);
                scheds[h] = new LocalTaskScheduler(h,
                        this.earlyStarts[h],
                        this.lastEnds[h],
                        this.capacities,
                        this.cHosters,
                        this.cUsages,
                        this.cEnds,
                        outs[h],
                        this.dHosters,
                        this.dUsages,
                        this.dStarts,
                        this.vIns[h],
                        assocs,
                        revAssociations,
                        aCause
                );
            }

            watchDTask = earlyStarts[0].getSolver().getEnvironment().makeInt(0);
            watchHosts = new BitSet(nbHosts);
        }

        @Override
        public ESat isEntailed() {

            //A hashmap to save the changes of each node (relatives to the previous moment) and each dimension
            TIntIntHashMap[][] changes = new TIntIntHashMap[nbHosts][nbDims];
            int[][] initFree = new int[nbHosts][];
            for (int h = 0; h < nbHosts; h++) {
                for (int d = 0; d < nbDims; d++) {
                    changes[h][d] = new TIntIntHashMap();
                }
                initFree[h] = Arrays.copyOf(capacities[h], capacities[h].length);
            }

            // check dStart[dt] >= earlyStart[dHost[dt]] for all dTasks
            for (int dt = 0; dt < dHosters.length; dt++) {
                if (!dHosters[dt].isInstantiated() || !dStarts[dt].isInstantiated()) {
                    return ESat.UNDEFINED;
                }
                int h = dHosters[dt].getValue();
                int t = dStarts[dt].getValue();
                if (t < earlyStarts[h].getValue()) {
                    LOGGER.error("D-slice {} arrives too early: {}. Min expected: {}", dHosters[dt], t, earlyStarts[h]);
                    return ESat.FALSE;
                }
                for (int d = 0; d < nbDims; d++) {
                    changes[h][d].put(t, changes[h][d].get(t) - dUsages[dt][d]);
                }
            }

            // check cEnd[ct] <= lastEnd[cHost[ct]] for all cTasks
            for (int ct = 0; ct < cHosters.length; ct++) {
                if (!cHosters[ct].isInstantiated() || !cEnds[ct].isInstantiated()) {
                    return ESat.UNDEFINED;
                }
                int h = cHosters[ct].getValue();
                int t = cEnds[ct].getValue();
                if (t > lastEnds[h].getValue()) {
                    LOGGER.error("C-slice {} leaves too late: {}. Max expected: {}", cHosters[ct], t, lastEnds[h]);
                    return ESat.FALSE;
                }
                for (int d = 0; d < nbDims; d++) {
                        changes[h][d].put(t, changes[h][d].get(t) + cUsages[ct][d]);
                        initFree[h][d] -= cUsages[ct][d];
                }
            }

            // check resource profile on each host
            boolean ok = true;
            for (int h = 0; h < nbHosts; h++) {
                TIntObjectHashMap<int[]> myChanges = myChanges(changes[h]);
                LOGGER.debug("--- Resource {} isSatisfied() ? ---", h);
                LOGGER.debug(" before: {}/{} {}changes: "
                        , Arrays.toString(initFree[h])
                        , Arrays.toString(capacities[h])
                        , prettyChanges(myChanges));

                int[] moments = myChanges.keys(new int[myChanges.size()]);
                Arrays.sort(moments);
                for (int t : moments) {
                    boolean bad = false;
                    for (int d = 0; d < nbDims; d++) {
                        initFree[h][d] += myChanges.get(t)[d];
                        if (initFree[h][d] < 0) {
                            bad = true;
                        }
                    }
                    if (bad) {
                        LOGGER.info("/!\\ at {}: free={}", t, Arrays.toString(initFree[h]));
                        ok = false;
                        break;
                    }
                }

                if (LOGGER.isDebugEnabled()) {
                    for (int x = 0; x < cHosters.length; x++) {
                        if (cHosters[x].getValue() == h) {
                            LOGGER.debug(cEnds[x].getName() + " ends at " + cEnds[x].getValue() + " uses:" + Arrays.toString(cUsages[x]));
                        }
                    }
                    for (int x = 0; x < dHosters.length; x++) {
                        if (dHosters[x].getValue() == h) {
                            LOGGER.debug(dStarts[x].getName() + " starts at " + dStarts[x].getValue() + " uses:" + Arrays.toString(dUsages[x]));
                        }
                    }
                }
            }
            return ESat.eval(ok);
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            int freeDTask = watchDTask.get();
            if (freeDTask < dHosters.length && (!dHosters[freeDTask].isInstantiated() || updateVInsAndWatch(freeDTask))) {
                return;
            }
            watchHosts.set(0,nbHosts);
            do {
                for (int h = watchHosts.nextSetBit(0); h >= 0; h = watchHosts.nextSetBit(h+1)) {
                    scheds[h].propagate(watchHosts);
                    watchHosts.clear(h);
                }
            } while (!watchHosts.isEmpty());

        }

        private boolean updateVInsAndWatch(int dt) {
            while (dt<dHosters.length && dHosters[dt].isInstantiated()) {
                int h = dHosters[dt].getValue();
                vIns[h].add(dt);
                dt++;
            }
            watchDTask.set(dt);
            return dt < dHosters.length;
        }


        private TIntObjectHashMap<int[]> myChanges(TIntIntHashMap[] change) {
            TIntObjectHashMap<int[]> map = new TIntObjectHashMap<>();
            for (int d = 0; d < nbDims; d++) {
                for (int t : change[d].keys()) {
                    int[] upd = map.get(t);
                    if (upd == null) {
                        upd = new int[nbDims];
                        map.put(t, upd);
                    }
                    upd[d] += change[d].get(t);
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


    }
}
