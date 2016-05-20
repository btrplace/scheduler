/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
import org.chocosolver.memory.IStateBool;
import org.chocosolver.memory.IStateIntVector;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.BitSet;

/**
 * @author Fabien Hermenier
 */
public class LocalTaskScheduler {

    public static final int DEBUG = -3;
    public static final int DEBUG_ALL = -2;
    public static final int NO_ASSOCIATIONS = -1;
    private static final Logger LOGGER = LoggerFactory.getLogger("solver");
    private int me;
    /**
     * out[i] = true <=> the consuming slice i will leave me.
     */
    private BitSet out;

    //The indexes of the slices that will leave me
    private int[] outIdx;
    /**
     * The moment the consuming slices ends. Same order as the hosting variables.
     */
    private IntVar[] cEnds;
    private IStateIntVector vIn;
    private final IntVar[] cHosters;

    /*
     * The moment the demanding slices ends. Same order as the hosting variables.
     */
    private IntVar[] dStarts;
    private final IntVar[] dHosters;

    private int[] startupFree;
    private int[] associateCTask;
    private int[] associateDTask;
    private int[] sortedMinProfile;

    private TIntIntHashMap[] profilesMin;

    private TIntIntHashMap[] profilesMax;

    private int[] sortedMaxProfile;

    private int[][] capacities;

    private int[][] cUsages;

    private int[][] dUsages;

    private int nbDims;

    private IntVar early;

    private IntVar last;

    private Propagator<?> aCause;

    private IStateBool entailed;

    public LocalTaskScheduler(int me,
                              IntVar early,
                              IntVar last,
                              int[][] capacities,
                              IntVar[] cHosters,
                              int[][] cUsages,
                              IntVar[] cEnds,
                              BitSet outs,
                              IntVar[] dHosters,
                              int[][] dUsages,
                              IntVar[] dStarts,
                              IStateIntVector vIn,
                              int[] associateCTask,
                              int[] associateDTask,
                              Propagator<?> iCause) {
        this.early = early;
        this.last = last;
        this.aCause = iCause;
        this.associateCTask = associateCTask;
        this.me = me;
        this.cEnds = cEnds;
        this.cHosters = cHosters;

        this.capacities = capacities;
        this.cUsages = cUsages;
        this.dUsages = dUsages;

        this.nbDims = capacities[0].length;
        assert this.cUsages.length == 0 || this.cUsages[0].length == nbDims;
        assert this.dUsages.length == 0 || this.dUsages[0].length == nbDims;

        this.dStarts = dStarts;
        this.dHosters = dHosters;
        this.vIn = vIn;
        this.out = outs;
        this.associateDTask = associateDTask;

        outIdx = new int[out.cardinality()];
        int x = 0;
        for (int ct = out.nextSetBit(0); ct >= 0; ct = out.nextSetBit(ct + 1)) {
            outIdx[x++] = ct;
        }

        //The amount of free resources at startup

        startupFree = new int[nbDims];
        profilesMax = new TIntIntHashMap[nbDims];
        profilesMin = new TIntIntHashMap[nbDims];
        for (int d = 0; d < nbDims; d++) {
            startupFree[d] = capacities[me][d];
            profilesMax[d] = new TIntIntHashMap();
            profilesMin[d] = new TIntIntHashMap();
        }

        int lastInf = outIdx.length == 0 ? 0 : Integer.MAX_VALUE;
        int lastSup = 0;

        for (int ct : outIdx) {
            for (int d = 0; d < nbDims; d++) {
                startupFree[d] -= cUsages[ct][d];
            }

            int i = cEnds[ct].getLB();
            int s = cEnds[ct].getUB();
            if (i < lastInf) {
                lastInf = i;
            }
            if (s > lastSup) {
                lastSup = s;
            }
        }

        entailed = early.getSolver().getEnvironment().makeBool(false);
    }

    /**
     * Translation for a relatives resources changes to an absolute free resources.
     *
     * @param changes       the map that indicates the free CPU variation
     * @param sortedMoments the different moments sorted in ascending order
     */
    private static void toAbsoluteFreeResources(TIntIntHashMap changes, int[] sortedMoments) {
        for (int i = 1; i < sortedMoments.length; i++) {
            int t = sortedMoments[i];
            int lastT = sortedMoments[i - 1];
            int lastFree = changes.get(lastT);

            changes.put(t, changes.get(t) + lastFree);
        }
    }


    public void propagate(BitSet watchHosts) throws ContradictionException {
        if (vIn.size() == 0 && outIdx.length == 0) {
            return;
        }

        if (entailed.get()) {
            return;
        }
        boolean allInstantiated = computeProfiles();

        checkInvariant();
        if (allInstantiated) {
            entailed.set(true);
            return;
        }

        updateCEndsSup(watchHosts);
        updateDStartsInf(watchHosts);
        updateDStartsSup(watchHosts);
    }

    /**
     * Report if the current local constraint is entailed or not.
     *
     * @return {@code true} iff entailed
     */
    public boolean isEntailed() {
        return entailed.get();
    }

    private boolean computeProfiles() throws ContradictionException {

        initProfile();

        boolean allinstantiated = insertCSlices();
        allinstantiated &= insertDSlices();

        //Now transforms into an absolute profile
        absoluteValues();

        summary();
        return allinstantiated;
    }

    private void absoluteValues() {
        sortedMinProfile = profilesMin[0].keys();
        Arrays.sort(sortedMinProfile);

        sortedMaxProfile = profilesMax[0].keys();

        Arrays.sort(sortedMaxProfile);

        for (int d = 0; d < nbDims; d++) {
            toAbsoluteFreeResources(profilesMin[d], sortedMinProfile);
            toAbsoluteFreeResources(profilesMax[d], sortedMaxProfile);
        }
    }

    private boolean insertDSlices() throws ContradictionException {
        boolean allinstantiated = true;
        int lastSup = 0;
        for (int x = 0; x < vIn.size(); x++) {
            int dt = vIn.get(x);

            dStarts[dt].updateLowerBound(early.getLB(), aCause);
            allinstantiated &= dStarts[dt].isInstantiated() || associatedToCSliceOnCurrentNode(dt);


            int tl = dStarts[dt].getLB();
            int tu = dStarts[dt].getUB();
            if (tu > lastSup) {
                lastSup = tu;
            }

            for (int d = 0; d < nbDims; d++) {
                profilesMin[d].put(tu, profilesMin[d].get(tu) + dUsages[dt][d]);
                profilesMax[d].put(tl, profilesMax[d].get(tl) + dUsages[dt][d]);
            }
        }
        early.updateUpperBound(lastSup, aCause);
        return allinstantiated;
    }

    private boolean insertCSlices() throws ContradictionException {
        boolean allinstantiated = true;
        // the cTasks
        int lastInf = 0;
        for (int ct : outIdx) {

            cEnds[ct].updateUpperBound(last.getUB(), aCause);
            allinstantiated &= cEnds[ct].isInstantiated() || associatedToDSliceOnCurrentNode(ct);

            int tu = cEnds[ct].getUB();
            int tl = cEnds[ct].getLB();
            lastInf = Math.max(tl, lastInf);

            // the cTask does not migrate and its demand increases on at least one dimension
            boolean increasing = associatedToDSliceOnCurrentNode(ct) && increase(ct, associateDTask[ct]);

            for (int d = 0; d < nbDims; d++) {
                int diff = cUsages[ct][d];
                if (increasing) {
                    profilesMax[d].put(tl, profilesMax[d].get(tl) - diff);
                    profilesMin[d].put(tu, profilesMin[d].get(tu) - diff);
                } else {
                    //the cTask free resources (by migration or decreasing demand on dimensions
                    profilesMin[d].put(tl, profilesMin[d].get(tl) - diff);
                    profilesMax[d].put(tu, profilesMax[d].get(tu) - diff);
                }
            }
        }
        last.updateLowerBound(lastInf, aCause);
        return allinstantiated;
    }

    private void initProfile() {
        for (int d = 0; d < nbDims; d++) {
            //What is necessarily used on the resource
            profilesMin[d].clear();

            //Maximum possible usage on the resource
            profilesMax[d].clear();

            profilesMax[d].put(0, capacities[me][d] - startupFree[d]);
            profilesMin[d].put(0, capacities[me][d] - startupFree[d]);
        }
    }

    private void summary() {
        if (me == DEBUG || DEBUG == DEBUG_ALL) {
            LOGGER.debug("---" + me + "--- startupFree=" + Arrays.toString(startupFree)
                    + " init=" + Arrays.toString(capacities[me]) + "; early=" + early.toString() + "; last=" + last.toString());
            for (int x = 0; x < vIn.size(); x++) {
                int i = vIn.get(x);
                LOGGER.debug((dStarts[i].isInstantiated() ? "!" : "?") + " " + dStarts[i].toString() + " " + Arrays.toString(dUsages[i]));
            }

            for (int i : outIdx) {
                LOGGER.debug((cEnds[i].isInstantiated() ? "!" : "?") + " " + cEnds[i].toString() + " " + Arrays.toString(cUsages[i]));
            }


            for (int i = 0; i < nbDims; i++) {
                LOGGER.debug("profileMin dim " + i + "=" + prettyProfile(sortedMinProfile, profilesMin[i]));
                LOGGER.debug("profileMax dim " + i + "=" + prettyProfile(sortedMaxProfile, profilesMax[i]));
            }
            LOGGER.debug("/--- " + me + "---/");
        }
    }
    private boolean increase(int ct, int dt) {
        for (int d = 0; d < nbDims; d++) {
            if (dUsages[dt][d] > cUsages[ct][d]) {
                return true;
            }
        }
        return false;
    }

    private boolean associatedToDSliceOnCurrentNode(int cSlice) {
        return associateDTask[cSlice] != NO_ASSOCIATIONS && dHosters[associateDTask[cSlice]].isInstantiatedTo(me);
    }

    private boolean associatedToCSliceOnCurrentNode(int dSlice) {
        return associateCTask[dSlice] != NO_ASSOCIATIONS && out.get(associateCTask[dSlice]);
    }

    private static String prettyProfile(int[] ascMoments, TIntIntHashMap prof) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < ascMoments.length; i++) {
            int t = ascMoments[i];
            b.append(t);
            b.append(':');
            b.append(prof.get(t));
            if (i != ascMoments.length - 1) {
                b.append(' ');
            }
        }
        return b.toString();
    }

    private boolean checkInvariant() throws ContradictionException {
        for (int t : sortedMinProfile) {
            for (int d = 0; d < nbDims; d++) {
                if (profilesMin[d].get(t) > capacities[me][d]) {
                    if (me == DEBUG || DEBUG == DEBUG_ALL) {
                        LOGGER.debug("(" + me + ") Invalid min profile at " + t + " on dimension " + d
                                + ": " + profilesMin[d].get(t) + " > " + capacities[me][d]);
                    }
                    aCause.contradiction(early, "");
                }
            }
        }
        return false;
    }

    // TODO: ou sont instanciees les dates des VMs qui restent sur le noeud ??
    // TODO: mettre passif quand toutes les dates in et out sont instanciees
    // TODO: detecter si la capacite totale permet de faire passer tout le monde (et mettre passif dans ce cas)

    private void updateDStartsInf(BitSet watchHosts) throws ContradictionException {

        for (int idx = 0; idx < vIn.size(); idx++) {
            int i = vIn.get(idx);
            if (!dStarts[i].isInstantiated() && !associatedToCSliceOnCurrentNode(i)) {

                int lastT = -1;
                for (int x = sortedMinProfile.length - 1; x >= 0; x--) {
                    int t = sortedMinProfile[x];
                    if (t <= dStarts[i].getLB()) {
                        break;
                    }
                    int prevT = sortedMinProfile[x - 1];
                    if (t <= dStarts[i].getUB()
                            && exceedCapacity(profilesMin, prevT, dUsages[i])) {
                        lastT = t;
                        break;
                    }
                }
                if (dStarts[i].updateLowerBound(Math.max(lastT, early.getLB()), aCause) && associateCTask[i] != NO_ASSOCIATIONS && dStarts[i].isInstantiated()) {
                    watchHosts.set(cHosters[associateCTask[i]].getValue());
                }
            }
        }
    }

    private void updateDStartsSup(BitSet watchHosts) throws ContradictionException {

        int lastSup = -1;
        for (int i = sortedMaxProfile.length - 1; i >= 0; i--) {
            int t = sortedMaxProfile[i];
            if (!exceedCapacity(profilesMax, t, capacities[me])) {
                lastSup = t;
            } else {
                break;
            }
        }
        if (lastSup != -1) {
            for (int x = 0; x < vIn.size(); x++) {
                int i = vIn.get(x);
                if (!dStarts[i].isInstantiated() && !associatedToCSliceOnCurrentNode(i)) {
                    int s = Math.max(dStarts[i].getLB(), lastSup);
                    if (dStarts[i].updateUpperBound(s, aCause) && associateCTask[i] != NO_ASSOCIATIONS && dStarts[i].isInstantiated()) {
                        watchHosts.set(cHosters[associateCTask[i]].getValue());
                    }
                }
            }
        }
    }

    private void updateCEndsSup(BitSet watchHosts) throws ContradictionException {
        for (int i : outIdx) {
            if (!cEnds[i].isInstantiated() && !associatedToDSliceOnCurrentNode(i)) {

                int lastT = -1;
                for (int t : sortedMinProfile) {
                    if (t >= cEnds[i].getUB()) {
                        break;
                    } else if (t >= cEnds[i].getLB() &&
                            exceedCapacity(profilesMin, t, cUsages[i])) {
                        lastT = t;
                        break;
                    }
                }
                if (cEnds[i].updateUpperBound((lastT != -1) ? Math.min(lastT, last.getUB()) : last.getUB(), aCause) && associateDTask[i] != NO_ASSOCIATIONS && cEnds[i].isInstantiated()) {
                    watchHosts.set(dHosters[associateDTask[i]].getValue());
                }

            }
        }
    }

    private boolean exceedCapacity(TIntIntHashMap[] profiles, int t, int[] usage) {
        for (int d = 0; d < nbDims; d++) {
            if (profiles[d].get(t) + usage[d] > capacities[me][d]) {
                return true;
            }
        }
        return false;
    }

}
