/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

      private final AliasedCumulativesFiltering resource;

      private final IntVar[] cHosters;

      private final IntVar[] cEnds;

      private final IntVar[] dHosters;

      private final IntVar[] dStarts;

      private final int nbDims;

      private final int[] capacities;

      private final int[][] cUsages;

      private final int[][] dUsages;

      private final IStateIntVector vIns;

      private final IStateInt vInsSize;
      /**
       * 0 [0,1,2,4]
       * 1 [0,1,2,4]
       * 2 [2]
       * 3 [3,5]
       * 4 [0,1,2,4]
       * 5 [3,5]
       */
      private final TIntHashSet alias;

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

            this.vIns = cHosters[0].getModel().getEnvironment().makeIntVector(0, 0);
            vInsSize = cHosters[0].getModel().getEnvironment().makeInt(0);

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
                    vInsSize,
                    assocs,
                    revAssociations,
                    this);
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

            return isSatisfied(dHostersVals, dStartsVals, cHostersVals, cEndsVals);
        }

        @Override
        public int getPropagationConditions(int idx) {
            return IntEventType.INSTANTIATE.getMask();
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            if (PropagatorEventType.isFullPropagation(evtmask)) {
                this.toInstantiate = cHosters[0].getModel().getEnvironment().makeInt(dHosters.length);

                //Check whether some hosting variable are already instantiated
                for (int i = 0; i < dHosters.length; i++) {
                    if (dHosters[i].isInstantiated()) {
                        int nIdx = dHosters[i].getValue();
                        if (isIn(nIdx)) {
                            toInstantiate.add(-1);
                            vIns.add(i);
                            vInsSize.add(1);
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
                        fails();
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
                    vInsSize.add(1);
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

            return isSatisfied(dHostersVals, dStartsVals, cHostersVals, cEndsVals);
        }

        private ESat isSatisfied(int[] dHostersVals, int[] dStartsVals, int[] cHostersVals, int[] cEndsVals) {
            //A map to save the changes of the resource (relatives to the previous moment) in the resources distribution
            TIntIntHashMap[] changes = new TIntIntHashMap[nbDims];
            int[] currentFree = Arrays.copyOf(capacities, capacities.length);

            for (int i = 0; i < nbDims; i++) {
                changes[i] = new TIntIntHashMap();
                for (int j = 0; j < dHostersVals.length; j++) {
                    //for each placed dSlices, we get the used resource and the moment the slice arrives on it
                    if (isIn(dHostersVals[j])) {
                        changes[i].put(dStartsVals[j], changes[i].get(dStartsVals[j]) - dUsages[i][j]);
                    }
                }

                for (int j = 0; j < cHostersVals.length; j++) {
                    if (isIn(cHostersVals[j])) {
                        changes[i].put(cEndsVals[j], changes[i].get(cEndsVals[j]) + cUsages[i][j]);
                        currentFree[i] -= cUsages[i][j];
                    }
                }

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
