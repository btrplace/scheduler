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

package org.btrplace.scheduler.choco.extensions;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.memory.IStateIntVector;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.BitSet;

/**
 * A kind of cumulatives constraint where a single resource is shared among multiple identifiers.
 *
 * @author Fabien Hermenier
 * @see org.btrplace.scheduler.choco.extensions.TaskScheduler
 */
public class AliasedCumulatives extends Constraint {

    /**
     * Make a new constraint.
     *
     * @param alias    the resource identifier related to this cumulative
     * @param capas    for each dimension, the capacity of each resource
     * @param cHosters the placement variable of each cTask
     * @param cUsages  the resource usage of each cTask for each dimension
     * @param cEnds    the moment each cTask ends
     * @param dHosters the placement variable of each dTask
     * @param dUsages  the resource usage of each dTask for each dimension
     * @param dStarts  the moment each dTask starts
     * @param assocs   indicate association between cTasks and dTasks. Associated tasks cannot overlap on a same resource
     */
    public AliasedCumulatives(int[] alias,
                              int[] capas,
                              IntVar[] cHosters,
                              int[][] cUsages,
                              IntVar[] cEnds,
                              IntVar[] dHosters,
                              int[][] dUsages,
                              IntVar[] dStarts,
                              int[] assocs) {

        super("AliasedCumulatives", new AliasedCumulativesPropagator(alias, capas, cHosters, cUsages, cEnds, dHosters, dUsages, dStarts, assocs));
    }

    static class AliasedCumulativesPropagator extends Propagator<IntVar> {

        private AliasedCumulativesFiltering resource;

        private IntVar[] cHosters;

        private IntVar[] cEnds;

        private IntVar[] dHosters;

        private IntVar[] dStarts;

        private int nbDims;

        private int[] capacities;

        private int[][] cUsages;

        private int[][] dUsages;

        private IStateIntVector vIns;

        /**
         * 0 [0,1,2,4]
         * 1 [0,1,2,4]
         * 2 [2]
         * 3 [3,5]
         * 4 [0,1,2,4]
         * 5 [3,5]
         */
        private TIntHashSet alias;

        private IStateInt toInstantiate;

        public AliasedCumulativesPropagator(int[] alias,
                                            int[] capas,
                                            IntVar[] cHosters,
                                            int[][] cUsages,
                                            IntVar[] cEnds,
                                            IntVar[] dHosters,
                                            int[][] dUsages,
                                            IntVar[] dStarts,
                                            int[] assocs) {
            super(ArrayUtils.append(dHosters, cHosters, cEnds, dStarts), PropagatorPriority.VERY_SLOW, true);
            this.alias = new TIntHashSet(alias);
            this.cHosters = cHosters;
            this.dHosters = dHosters;
            this.cEnds = cEnds;
            this.dStarts = dStarts;

            this.capacities = capas;
            this.cUsages = cUsages;
            this.dUsages = dUsages;

            this.nbDims = capas.length;

            this.vIns = cHosters[0].getSolver().getEnvironment().makeIntVector(0, 0);


            BitSet out = new BitSet(cHosters.length);

            for (int i = 0; i < cHosters.length; i++) {
                int v = cHosters[i].getValue();
                if (isIn(v)) {
                    out.set(i);
                }
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


            resource = new AliasedCumulativesFiltering(
                    capacities,
                    cUsages,
                    cEnds,
                    out,
                    dUsages,
                    dStarts,
                    vIns,
                    assocs,
                    revAssociations,
                    aCause);
        }

        @Override
        public ESat isEntailed() {
            //Split this use tab to ease the analysis
            int[] dHostersVals = new int[dHosters.length];
            int[] cHostersVals = new int[cHosters.length];
            int[] dStartsVals = new int[dStarts.length];
            int[] cEndsVals = new int[cEnds.length];

            //dHosts, cHosts, cEnds, dStarts
            for (int i = 0; i < dHosters.length; i++) {
                dHostersVals[i] = vars[i].getValue();
                dStartsVals[i] = vars[i + dHosters.length + cHosters.length + cEnds.length].getValue();
            }

            for (int i = 0; i < cHosters.length; i++) {
                cHostersVals[i] = vars[i + dHosters.length].getValue();
                cEndsVals[i] = vars[i + dHosters.length + cHosters.length].getValue();
            }

            //A hashmap to save the changes of the resource (relatives to the previous moment) in the resources distribution
            TIntIntHashMap[] changes = new TIntIntHashMap[nbDims];

            for (int i = 0; i < nbDims; i++) {
                changes[i] = new TIntIntHashMap();
            }


            for (int i = 0; i < nbDims; i++) {
                for (int j = 0; j < dHostersVals.length; j++) {
                    //for each placed dSlices, we get the used resource and the moment the slice arrives on it
                    int nIdx = dHostersVals[j];
                    if (isIn(nIdx)) {
                        changes[i].put(dStartsVals[j], changes[i].get(dStartsVals[j]) - dUsages[i][j]);
                    }
                }
            }

            int[] currentFree = Arrays.copyOf(capacities, capacities.length);

            for (int i = 0; i < nbDims; i++) {
                for (int j = 0; j < cHostersVals.length; j++) {
                    int nIdx = cHostersVals[j];
                    if (isIn(nIdx)) {
                        changes[i].put(cEndsVals[j], changes[i].get(cEndsVals[j]) + cUsages[i][j]);
                        currentFree[i] -= cUsages[i][j];
                    }
                }
            }

            for (int i = 0; i < nbDims; i++) {
                //Now we check the evolution of the absolute free space.
                for (int x = 0; x < changes[i].keys().length; x++) {
                    currentFree[i] += changes[i].get(x);
                    if (currentFree[i] < 0) {
                        return ESat.FALSE;
                    }
                }
            }
            return ESat.TRUE;
        }

        @Override
        public int getPropagationConditions(int idx) {
            return IntEventType.INSTANTIATE.getMask();
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            if (PropagatorEventType.isFullPropagation(evtmask)) {
                this.toInstantiate = cHosters[0].getSolver().getEnvironment().makeInt(dHosters.length);

                //Check whether some hosting variable are already instantiated
                for (int i = 0; i < dHosters.length; i++) {
                    if (dHosters[i].isInstantiated()) {
                        int nIdx = dHosters[i].getValue();
                        if (isIn(nIdx)) {
                            toInstantiate.add(-1);
                            vIns.add(i);
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
                        if (!v.isInstantiated()) {
                            isFull = false;
                            break;
                        }
                    }
                    if (isFull && !resource.propagate()) {
                        contradiction(null, "");
                    }
                    for (IntVar v : vars) {
                        size -= v.getDomainSize();
                    }
                } while (size > 0);
            }
        }

        @Override
        public void propagate(int idx, int m) throws ContradictionException {
            if (idx < dHosters.length) {
                toInstantiate.add(-1);
                int nIdx = vars[idx].getValue();
                if (isIn(nIdx)) {
                    vIns.add(idx);
                }
            }
            forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
        }

        public ESat isSatisfied(int[] vals) {
            //Split this use tab to ease the analysis
            int[] dHostersVals = new int[dHosters.length];
            int[] cHostersVals = new int[cHosters.length];
            int[] dStartsVals = new int[dStarts.length];
            int[] cEndsVals = new int[cEnds.length];

            //dHosts, cHosts, cEnds, dStarts
            for (int i = 0; i < dHosters.length; i++) {
                dHostersVals[i] = vals[i];
                dStartsVals[i] = vals[i + dHosters.length + cHosters.length + cEnds.length];
            }

            for (int i = 0; i < cHosters.length; i++) {
                cHostersVals[i] = vals[i + dHosters.length];
                cEndsVals[i] = vals[i + dHosters.length + cHosters.length];
            }

            //A hashmap to save the changes of the resource (relatives to the previous moment) in the resources distribution
            TIntIntHashMap[] changes = new TIntIntHashMap[nbDims];

            for (int i = 0; i < nbDims; i++) {
                changes[i] = new TIntIntHashMap();
            }


            for (int i = 0; i < nbDims; i++) {
                for (int j = 0; j < dHostersVals.length; j++) {
                    //for each placed dSlices, we get the used resource and the moment the slice arrives on it
                    int nIdx = dHostersVals[j];
                    if (isIn(nIdx)) {
                        changes[i].put(dStartsVals[j], changes[i].get(dStartsVals[j]) - dUsages[i][j]);
                    }
                }
            }

            int[] currentFree = Arrays.copyOf(capacities, capacities.length);

            for (int i = 0; i < nbDims; i++) {
                for (int j = 0; j < cHostersVals.length; j++) {
                    int nIdx = cHostersVals[j];
                    if (isIn(nIdx)) {
                        changes[i].put(cEndsVals[j], changes[i].get(cEndsVals[j]) + cUsages[i][j]);
                        currentFree[i] -= cUsages[i][j];
                    }
                }
            }

            for (int i = 0; i < nbDims; i++) {
                //Now we check the evolution of the absolute free space.
                for (int x = 0; x < changes[i].keys().length; x++) {
                    currentFree[i] += changes[i].get(x);
                    if (currentFree[i] < 0) {
                        return ESat.FALSE;
                    }
                }
            }
            return ESat.TRUE;
        }

        private boolean isIn(int idx) {
            return alias.contains(idx);
        }

    }
}
