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
import gnu.trove.TIntObjectHashMap;

import java.util.Arrays;
import java.util.BitSet;
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

    /**
     * Make a new constraint.
     *
     * @param env         the solver environment
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

        Arrays.toString(capacities);
        Arrays.toString(cUsages);
        Arrays.toString(dUsages);

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
        //ChocoLogging.getBranchingLogger().info("awakeOnInst(" + vars[idx] + ")");
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
            if (!sc.checkInvariant()) {
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

        //A hashmap to save the changes of each node (relatives to the previous moment) and each dimension
        TIntIntHashMap[][] changes = new TIntIntHashMap[nbDims][nbResources];

        for (int d = 0; d < nbDims; d++) {
            for (int j = 0; j < nbResources; j++) {
                changes[d][j] = new TIntIntHashMap();
            }
        }


        for (int d = 0; d < nbDims; d++) { //Each dimension
            for (int j = 0; j < dHostersVals.length; j++) { //for each placed dSlices
                int r = dHostersVals[j]; //on which resource it is placed
                int st = dStartsVals[j];
                //ChocoLogging.getBranchingLogger().info("d= "+ d + ", j=" + dHosters[j].pretty() + " j.val=" + r + ", " + "start=" + dStarts[j].pretty() + " val=" + st);
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
            ChocoLogging.getBranchingLogger().finest("--- Resource " + j + " isSatisfied() ? ---");
            ChocoLogging.getBranchingLogger().finest(" before: " + prettyUsages(currentFree, j)
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
                    ChocoLogging.getMainLogger().info("/!\\ at " + t + ": free=" + prettyUsages(currentFree, j));
                    ok = false;
                    break;
                }
            }

            if (ChocoLogging.getBranchingLogger().isLoggable(Level.FINEST)) {
                for (int x = 0; x < cHostersVals.length; x++) {
                    if (cHostersVals[x] == j) {
                        ChocoLogging.getBranchingLogger().finest(cEnds[x].getName() + " ends at " + cEndsVals[x] + " uses:" + prettyUsages(cUsages, x));
                    }
                }
                for (int x = 0; x < dHostersVals.length; x++) {
                    if (dHostersVals[x] == j) {
                        ChocoLogging.getBranchingLogger().finest(dStarts[x].getName() + " starts at " + dStartsVals[x] + " uses:" + prettyUsages(dUsages, x));
                    }
                }
            }
        }
        return ok;
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
            //ChocoLogging.getBranchingLogger().info("rc " + nIdx + " changes for d=" + d + ": " + ch);
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

    private String pretty(int[][] arr) {
        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int[] x : arr) {
            b.append(Arrays.toString(x));
        }
        b.append(']');
        return b.toString();
    }
}
