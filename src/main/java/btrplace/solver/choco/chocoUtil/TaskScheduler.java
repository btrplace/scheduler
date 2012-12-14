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
import gnu.trove.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;

/**
 * A constraint to schedule tasks with regards to their resource usages on resources having a finite amount to share.
 * Tasks and resources can have multiple dimensions.
 * There is only 2 kind of tasks. cTasks that are already placed and necessarily starts at 0 and dTasks that
 * are not placed but end necessarily at the end of the schedule.
 * Inspired by the cumulatives constraint.
 *
 * @author Fabien Hermenier
 */
public class TaskScheduler extends AbstractLargeIntSConstraint {

    private LocalTaskScheduler[] scheds;

    private IntDomainVar[] cHosters;

    private IntDomainVar[] cEnds;

    private IntDomainVar[] dHosters;

    private IntDomainVar[] dStarts;

    private int nbResources;

    private int nbDims;

    private int[][] capacities;

    private int[][] cUsages;

    private int[][] dUsages;

    private IStateInt toInstantiate;

    private IEnvironment env;

    private IStateIntVector[] vIns;

    public static IntDomainVar[] allButNull(IntDomainVar[] xs) {
        List<IntDomainVar> l = new ArrayList<IntDomainVar>();
        for (int i = 0; i < xs.length; i++) {
            if (xs[i] != null) {
                l.add(xs[i]);
            }
        }
        return l.toArray(new IntDomainVar[l.size()]);
    }

    public TaskScheduler(IEnvironment env,
                         IntDomainVar[] earlyStarts,
                         IntDomainVar[] lastEnds,
                         int[][] capas,
                         IntDomainVar[] cHosters,
                         int[][] cUsages,
                         IntDomainVar[] cEnds,
                         IntDomainVar[] dHosters,
                         int[][] dUsages,
                         IntDomainVar[] dStarts,
                         int[] assocs) {

        super(ArrayUtils.append(dHosters, cHosters, cEnds, dStarts, earlyStarts, lastEnds));
        this.env = env;
        this.cHosters = cHosters;
        this.dHosters = dHosters;
        this.cEnds = cEnds;
        this.dStarts = dStarts;

        this.capacities = capas;
        this.cUsages = cUsages;
        this.dUsages = dUsages;

        this.nbResources = capas[0].length;
        this.nbDims = capas.length;
        int nbCTasks = cUsages[0].length;

        scheds = new LocalTaskScheduler[nbResources];

        BitSet[] outs = new BitSet[scheds.length];
        for (int i = 0; i < scheds.length; i++) {
            outs[i] = new BitSet(cHosters.length);
        }

        for (int i = 0; i < cHosters.length; i++) {
            outs[cHosters[i].getVal()].set(i);
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

        this.vIns = new IStateIntVector[scheds.length];
        for (int i = 0; i < scheds.length; i++) {
            vIns[i] = env.makeIntVector();
            scheds[i] = new LocalTaskScheduler(i, env,
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
                    revAssociations
            );
        }
    }

    @Override
    public void awake() throws ContradictionException {

        this.toInstantiate = env.makeInt(dHosters.length);

        //Check whether some hosting variable are already instantiated
        for (int i = 0; i < dHosters.length; i++) {
            if (dHosters[i].isInstantiated()) {
                int nIdx = dHosters[i].getVal();
                toInstantiate.add(-1);
                vIns[nIdx].add(i);
            }
        }
    }

    @Override
    public void propagate() throws ContradictionException {
        if (isFull2()) {
            for (int i = 0; i < scheds.length; i++) {
                if (!scheds[i].propagate()) {
                    fail();
                }
            }
        }
    }

    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        if (idx < dHosters.length) {
            toInstantiate.add(-1);
            int nIdx = vars[idx].getVal();
            vIns[nIdx].add(idx);
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
        for (LocalTaskScheduler sc : scheds) {
            sc.computeProfiles();
            try {
                if (!sc.checkInvariant()) {
                    return false;
                }
            } catch (ContradictionException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isSatisfied(int[] vals) {
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

        //A hashmap to save the changes of each node (relatives to the previous moment) in the resources distribution
        TIntIntHashMap[][] changes = new TIntIntHashMap[nbDims][nbResources];

        for (int i = 0; i < nbDims; i++) {
            for (int j = 0; j < nbResources; j++) {
                changes[i][j] = new TIntIntHashMap();
            }
        }


        for (int i = 0; i < nbDims; i++) { //Each dimension
            for (int j = 0; j < dHostersVals.length; j++) { //for each placed dSlices
                int nIdx = dHostersVals[j]; //on which resource it is placed
                changes[i][nIdx].put(dStartsVals[j], changes[i][nIdx].get(dStartsVals[j]) - dUsages[i][j]);
            }
        }

        int[][] currentFree = new int[nbDims][];
        for (int i = 0; i < currentFree.length; i++) {
            currentFree[i] = Arrays.copyOf(capacities[i], capacities[i].length);
        }

        for (int i = 0; i < nbDims; i++) {
            for (int j = 0; j < cHostersVals.length; j++) {
                int nIdx = cHostersVals[j];
                changes[i][nIdx].put(cEndsVals[j], changes[i][nIdx].get(cEndsVals[j] + cUsages[i][j]));
                currentFree[i][nIdx] -= cUsages[i][j];
            }
        }


        if (ChocoLogging.getBranchingLogger().isLoggable(Level.FINEST)) {
            for (int x = 0; x < cHostersVals.length; x++) {
                ChocoLogging.getBranchingLogger().finest(x + " " + cEnds[x].pretty() + " ends at " + cEndsVals[x]);
            }
            for (int x = 0; x < dHostersVals.length; x++) {
                ChocoLogging.getBranchingLogger().finest(dStarts[x].pretty());
            }
        }

        boolean ok = true;
        for (int j = 0; j < nbResources; j++) {
            ChocoLogging.getBranchingLogger().finest("--- Resource " + j + " isSatisfied() ---");
            for (int i = 0; i < nbDims; i++) {
                ChocoLogging.getBranchingLogger().finest("Dimension " + (i + 1) + "/" + nbDims + ": "
                        + " currentFree= " + currentFree[i][j]
                        + " changes= " + changes[i][j]);
                //Now we check the evolution of the absolute free space.

                for (int x = 0; x < changes[i][j].keys().length; x++) {
                    currentFree[i][j] += changes[i][j].get(x);
                    if (currentFree[i][j] < 0) {
                        ChocoLogging.getMainLogger().severe("-> free@" + x + ": " + currentFree[i][j]);
                        ok = false;
                    }
                }
            }
        }
        return ok;
    }
}
