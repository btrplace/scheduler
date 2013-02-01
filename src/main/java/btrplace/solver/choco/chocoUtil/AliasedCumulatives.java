/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace.solver.choco.chocoUtil;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;

import java.util.Arrays;
import java.util.BitSet;

/**
 * A kind of cumulatives constraint where a single resource is shared among multiple identifiers.
 *
 * @author Fabien Hermenier
 * @see TaskScheduler
 */
public class AliasedCumulatives extends AbstractLargeIntSConstraint {

    private AliasedCumulativesFiltering resource;

    private IntDomainVar[] cHosters;

    private IntDomainVar[] cEnds;

    private IntDomainVar[] dHosters;

    private IntDomainVar[] dStarts;

    private int nbDims;

    private int[] capacities;

    private int[][] cUsages;

    private int[][] dUsages;

    private IStateInt toInstantiate;

    private IEnvironment env;

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

    /**
     * Make a new constraint.
     *
     * @param env      the solver environment
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
    public AliasedCumulatives(IEnvironment env,
                              int[] alias,
                              int[] capas,
                              IntDomainVar[] cHosters,
                              int[][] cUsages,
                              IntDomainVar[] cEnds,
                              IntDomainVar[] dHosters,
                              int[][] dUsages,
                              IntDomainVar[] dStarts,
                              int[] assocs) {

        super(ArrayUtils.append(dHosters, cHosters, cEnds, dStarts));
        this.alias = new TIntHashSet(alias);
        this.env = env;
        this.cHosters = cHosters;
        this.dHosters = dHosters;
        this.cEnds = cEnds;
        this.dStarts = dStarts;

        this.capacities = capas;
        this.cUsages = cUsages;
        this.dUsages = dUsages;

        this.nbDims = capas.length;
        int nbCTasks = cUsages[0].length;


        BitSet out = new BitSet(cHosters.length);

        for (int i = 0; i < cHosters.length; i++) {
            int v = cHosters[i].getVal();
            if (isIn(v)) {
                out.set(i);
            }
        }


        int[] revAssociations = new int[nbCTasks];
        for (int i = 0; i < revAssociations.length; i++) {
            revAssociations[i] = LocalTaskScheduler.NO_ASSOCIATIONS;
        }

        for (int i = 0; i < assocs.length; i++) {
            if (assocs[i] != LocalTaskScheduler.NO_ASSOCIATIONS) {
                revAssociations[assocs[i]] = i;
            }
        }

        vIns = env.makeIntVector();
        resource = new AliasedCumulativesFiltering(env,
                capacities,
                cUsages,
                cEnds,
                out,
                dUsages,
                dStarts,
                vIns,
                assocs,
                revAssociations);
    }

    private boolean isIn(int idx) {
        return alias.contains(idx);
    }

    @Override
    public void awake() throws ContradictionException {

        this.toInstantiate = env.makeInt(dHosters.length);

        //Check whether some hosting variable are already instantiated
        for (int i = 0; i < dHosters.length; i++) {
            if (dHosters[i].isInstantiated()) {
                int nIdx = dHosters[i].getVal();
                if (isIn(nIdx)) {
                    toInstantiate.add(-1);
                    vIns.add(i);
                }
            }
        }
    }

    @Override
    public void propagate() throws ContradictionException {
        if (isFull2() && !resource.propagate()) {
            fail();
        }
    }

    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        if (idx < dHosters.length) {
            toInstantiate.add(-1);
            int nIdx = vars[idx].getVal();
            if (isIn(nIdx)) {
                vIns.add(idx);
            }
        }
        this.constAwake(false);
    }

    private boolean isFull2() {
        return toInstantiate.get() == 0;
    }

    @Override
    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK;
    }

    @Override
    public boolean isSatisfied() {
        int[] vals = new int[vars.length];
        for (int i = 0; i < vals.length; i++) {
            vals[i] = vars[i].getVal();
        }
        return isSatisfied(vals);
    }

    @Override
    public boolean isConsistent() {
        resource.computeProfiles();
        return resource.checkInvariant();
    }

    @Override
    public boolean isSatisfied(int[] vals) {
        //Split this use tab to ease the analysis
        int[] dHostersVals = new int[dHosters.length];
        int[] cHostersVals = new int[cHosters.length];
        int[] dStartsVals = new int[dStarts.length];
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

        //A hashmap to save the changes of the resource (relatives to the previous moment) in the resources distribution
        TIntIntHashMap[] changes = new TIntIntHashMap[nbDims];

        for (int i = 0; i < nbDims; i++) {
            changes[i] = new TIntIntHashMap();
        }


        for (int i = 0; i < nbDims; i++) { //Each dimension
            for (int j = 0; j < dHostersVals.length; j++) { //for each placed dSlices
                int nIdx = dHostersVals[j]; //on which resource it is placed
                if (isIn(nIdx)) {
                    changes[i].put(dStartsVals[j], changes[i].get(dStartsVals[j]) - dUsages[i][j]);
                    ChocoLogging.getBranchingLogger().finest("Count dTask " + dHosters[j].getName() + " at " + dStartsVals[j]);
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
                    ChocoLogging.getBranchingLogger().finest("Count cTask " + cHosters[j].getName() + " at " + cEndsVals[j]);
                }
            }
        }


        /*if (ChocoLogging.getBranchingLogger().isLoggable(Level.FINEST)) {
            for (int x = 0; x < cHostersVals.length; x++) {
                ChocoLogging.getBranchingLogger().finest(cEnds[x].getName() + "=" + cEndsVals[x]);
            }
            for (int x = 0; x < dHostersVals.length; x++) {
                ChocoLogging.getBranchingLogger().finest(dStarts[x].getName() + "=" + dStartsVals[x]);
            }
        } */
        ChocoLogging.flushLogs();
        for (int i = 0; i < nbDims; i++) {
            ChocoLogging.getBranchingLogger().finest("AliasedCumulatives Dimension " + (i + 1) + "/" + nbDims + ": "
                    + " capacities=" + capacities[i] + ", free@0= " + currentFree[i]
                    + " changes= " + changes[i]);
            //Now we check the evolution of the absolute free space.

            for (int x = 0; x < changes[i].keys().length; x++) {
                currentFree[i] += changes[i].get(x);
                if (currentFree[i] < 0) {
                    ChocoLogging.getMainLogger().severe("-> free@" + x + ": " + currentFree[i]);
                    return false;
                }
            }
        }
        ChocoLogging.flushLogs();
        return true;
    }
}
