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
import memory.IStateInt;
import memory.IStateIntVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.ESat;

import java.util.Arrays;
import java.util.BitSet;

/**
 *
 * @author Fabien Hermenier
 */
public class LocalTaskScheduler {

    public static final int DEBUG = -3;
    public static final int DEBUG_ALL = -2;
    public static final int NO_ASSOCIATIONS = -1;
    private final Logger LOGGER = LoggerFactory.getLogger("solver");
    private int me;
    /**
     * out[i] = true <=> the consuming slice i will leave me.
     */
    private BitSet out;
    /**
     * The moment the consuming slices ends. Same order as the hosting variables.
     */
    private IntVar[] cEnds;
    private IStateIntVector vIn;
    /*
     * The moment the demanding slices ends. Same order as the hosting variables.
     */
    private IntVar[] dStarts;
    final private IntVar[] dHosters;

    private int[] startupFree;
    private int[] associations;
    private int[] revAssociations;
    private int[] sortedMinProfile;

    private TIntIntHashMap[] profilesMin;

    private TIntIntHashMap[] profilesMax;

    private int[] sortedMaxProfile;

    /**
     * LB of the moment the last c-slice leaves.
     */
    private IStateInt lastCendInf;

    /**
     * UB of the moment the last c-slice leaves.
     */
    private IStateInt lastCendSup;

    private int[][] capacities;

    private int[][] cUsages, dUsages;

    private int nbDims;

    private IntVar early, last;

    private ICause aCause;

    public LocalTaskScheduler(int me,
                              IntVar early,
                              IntVar last,
                              int[][] capacities,
                              int[][] cUsages,
                              IntVar[] cEnds,
                              BitSet outs,
                              IntVar[] dHosters,
                              int[][] dUsages,
                              IntVar[] dStarts,
                              IStateIntVector vIn,
                              int[] assocs,
                              int[] revAssocs,
                              ICause iCause) {
        this.early = early;
        this.last = last;
        this.aCause = iCause;
        this.associations = assocs;
        this.me = me;
        this.cEnds = cEnds;

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
        revAssociations = revAssocs;

        //The amount of free resources at startup

        startupFree = new int[nbDims];
        profilesMax = new TIntIntHashMap[nbDims];
        profilesMin = new TIntIntHashMap[nbDims];
        for (int d = 0; d < nbDims; d++) {
            startupFree[d] = capacities[me][d];
            profilesMax[d] = new TIntIntHashMap();
            profilesMin[d] = new TIntIntHashMap();
        }

        int lastInf = out.isEmpty() ? 0 : Integer.MAX_VALUE;
        int lastSup = 0;

        for (int ct = out.nextSetBit(0); ct >= 0; ct = out.nextSetBit(ct + 1)) {
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
        this.lastCendInf = early.getSolver().getEnvironment().makeInt(lastInf);
        this.lastCendSup = early.getSolver().getEnvironment().makeInt(lastSup);
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

    public ESat propagate() throws ContradictionException {
        computeProfiles();
        ESat idempotent = ESat.TRUE;
        last.updateLowerBound(lastCendInf.get(), aCause);
        // TODO check idempotence here ?

        if (!checkInvariant()) {
            return ESat.FALSE;
        }
        if (!updateCEndsSup()) idempotent = ESat.UNDEFINED;
        if (!updateDStartsInf()) idempotent = ESat.UNDEFINED;
        if (!updateDStartsSup()) idempotent = ESat.UNDEFINED;
        return idempotent;
    }

    public void computeProfiles() {

        for (int d = 0; d < nbDims; d++) {
            //What is necessarily used on the resource
            profilesMin[d] = new TIntIntHashMap();

            //Maximum possible usage on the resource
            profilesMax[d] = new TIntIntHashMap();

            profilesMax[d].put(0, capacities[me][d] - startupFree[d]);
            profilesMin[d].put(0, capacities[me][d] - startupFree[d]);
        }

        int lastInf = out.isEmpty() ? 0 : Integer.MAX_VALUE;
        int lastSup = 0;

        for (int ct = out.nextSetBit(0); ct >= 0; ct = out.nextSetBit(ct + 1)) {

            int t = cEnds[ct].getLB();
            if (t < lastInf) {
                lastInf = t;
            }
            boolean increasing = associatedToDSliceOnCurrentNode(ct) && increase(ct, revAssociations[ct]);
            if (increasing) {
                if (me == DEBUG || DEBUG == DEBUG_ALL) {
                    LOGGER.debug(me + " " + cEnds[ct].toString() + " increasing");
                }
                for (int d = 0; d < nbDims; d++) {
                    profilesMax[d].put(t, profilesMax[d].get(t) - cUsages[ct][d]);
                }

            } else {
                if (me == DEBUG || DEBUG == DEBUG_ALL) {
                    LOGGER.debug(me + " " + cEnds[ct].toString() + " < or non-associated (" + (revAssociations[ct] >= 0 ? dStarts[revAssociations[ct]].toString() : "no rev") + "?)");
                }
                for (int d = 0; d < nbDims; d++) {
                    profilesMin[d].put(t, profilesMin[d].get(t) - cUsages[ct][d]);
                }

            }

            t = cEnds[ct].getUB();
            if (t > lastSup) {
                lastSup = t;
            }
            if (increasing) {
                for (int d = 0; d < nbDims; d++) {
                    profilesMin[d].put(t, profilesMin[d].get(t) - cUsages[ct][d]);
                }
            } else {
                for (int d = 0; d < nbDims; d++) {
                    profilesMax[d].put(t, profilesMax[d].get(t) - cUsages[ct][d]);
                }
            }
        }
        if (out.isEmpty()) {
            lastInf = 0;
            lastSup = 0;
        }

        lastCendInf.set(lastInf);
        lastCendSup.set(lastSup);

        for (int d = 0; d < nbDims; d++) {
            for (int x = 0; x < vIn.size(); x++) {
                int j = vIn.get(x);
                int t = dStarts[j].getUB();
                profilesMin[d].put(t, profilesMin[d].get(t) + dUsages[j][d]);
                t = dStarts[j].getLB();
                profilesMax[d].put(t, profilesMax[d].get(t) + dUsages[j][d]);
            }
        }
        //Now transforms into an absolute profile
        sortedMinProfile = null;
        sortedMinProfile = profilesMin[0].keys();
        Arrays.sort(sortedMinProfile);

        sortedMaxProfile = null;
        sortedMaxProfile = profilesMax[0].keys();
        for (int i = 0; i < nbDims; i++) {
            profilesMax[i].keys(sortedMaxProfile);
        }

        Arrays.sort(sortedMaxProfile);

        for (int i = 0; i < nbDims; i++) {
            toAbsoluteFreeResources(profilesMin[i], sortedMinProfile);
            toAbsoluteFreeResources(profilesMax[i], sortedMaxProfile);
        }

        if (me == DEBUG || DEBUG == DEBUG_ALL) {
            LOGGER.debug("---" + me + "--- startupFree=" + Arrays.toString(startupFree)
                    + " init=" + Arrays.toString(capacities[me]) + "; early=" + early.toString() + "; last=" + last.toString());
            for (int x = 0; x < vIn.size(); x++) {
                int i = vIn.get(x);
                LOGGER.debug((dStarts[i].isInstantiated() ? "!" : "?") + " " + dStarts[i].toString() + " " + Arrays.toString(dUsages[i]));
            }

            for (int i = out.nextSetBit(0); i >= 0; i = out.nextSetBit(i + 1)) {
                LOGGER.debug((cEnds[i].isInstantiated() ? "!" : "?") + " " + cEnds[i].toString() + " " + Arrays.toString(cUsages[i]));
            }


            for (int i = 0; i < nbDims; i++) {
                LOGGER.debug("profileMin dim " + i + "=" + prettyProfile(sortedMinProfile, profilesMin[i]));
                LOGGER.debug("profileMax dim " + i + "=" + prettyProfile(sortedMaxProfile, profilesMax[i]));
            }
            LOGGER.debug("/--- " + me + "---/");
        }
    }

    private boolean increase(int x, int y) {
        for (int d = 0; d < nbDims; d++) {
            if (dUsages[y][d] > cUsages[x][d]) {
                return true;
            }
        }
        return false;
    }

    private boolean associatedToDSliceOnCurrentNode(int cSlice) {
        return revAssociations[cSlice] != NO_ASSOCIATIONS && dHosters[revAssociations[cSlice]].isInstantiatedTo(me);
    }

    private boolean associatedToCSliceOnCurrentNode(int dSlice) {
        return associations[dSlice] != NO_ASSOCIATIONS && out.get(associations[dSlice]);
    }

    private String prettyProfile(int[] ascMoments, TIntIntHashMap prof) {
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

    public boolean checkInvariant() {
        for (int x = 0; x < sortedMinProfile.length; x++) {
            int t = sortedMinProfile[x];
            for (int d = 0; d < nbDims; d++) {
                if (profilesMin[d].get(t) > capacities[me][d]) {
                    if (me == DEBUG || DEBUG == DEBUG_ALL) {
                        LOGGER.debug("(" + me + ") Invalid min profile at " + t + " on dimension " + d
                                + ": " + profilesMin[d].get(t) + " > " + capacities[me][d]);
                    }
                    return false;
                }
            }
        }

        //invariant related to the last and the early.
        for (int idx = 0; idx < vIn.size(); idx++) {
            int i = vIn.get(idx);
            if (dStarts[i].getUB() < early.getLB()) {
                if (me == DEBUG || DEBUG == DEBUG_ALL) {
                    LOGGER.debug("(" + me + ") The dSlice " + i + " starts too early (" + dStarts[i].toString() + ") (min expected=" + early.toString() + ")");
                }
                return false;
            }
        }

        for (int i = out.nextSetBit(0); i >= 0; i = out.nextSetBit(i + 1)) {
            if (cEnds[i].getLB() > last.getUB()) {
                if (me == DEBUG || DEBUG == DEBUG_ALL) {
                    LOGGER.debug("(" + me + ") The cSlice " + i + " ends too late (" + cEnds[i].toString() + ") (last expected=" + last.toString() + ")");
                }
                return false;
            }

        }
        return true;
    }

    private boolean updateDStartsInf() throws ContradictionException {

        boolean idempotent = true;
        for (int idx = 0; idx < vIn.size(); idx++) {
            int i = vIn.get(idx);
            if (!dStarts[i].isInstantiated() && !associatedToCSliceOnCurrentNode(i)) {
                if (DEBUG == me || DEBUG == DEBUG_ALL) {
                    LOGGER.debug("(" + me + ") - try to update lb of " + dStarts[i]);
                }

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
                dStarts[i].updateLowerBound(Math.max(lastT, early.getLB()), aCause);
                if (dStarts[i].isInstantiated()) idempotent = false;
            }
        }
        return idempotent;
    }

    private boolean updateDStartsSup() throws ContradictionException {

        boolean idempotent = true;
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
                    dStarts[i].updateUpperBound(s, aCause);
                    if (dStarts[i].isInstantiated()) idempotent = false;
                }
            }
        }
        return idempotent;
    }

    private boolean updateCEndsSup() throws ContradictionException {
        boolean idempotent = true;
        for (int i = out.nextSetBit(0); i >= 0; i = out.nextSetBit(i + 1)) {
            if (!cEnds[i].isInstantiated() && !associatedToDSliceOnCurrentNode(i)) {

                int lastT = -1;
                for (int x = 0; x < sortedMinProfile.length; x++) {
                    int t = sortedMinProfile[x];
                    if (t >= cEnds[i].getUB()) {
                        break;
                    } else if (t >= cEnds[i].getLB() &&
                            exceedCapacity(profilesMin, t, cUsages[i])) {
                        lastT = t;
                        break;
                    }
                }
                if (lastT != -1) {
                    cEnds[i].updateUpperBound(Math.min(lastT, last.getUB()), aCause);
                } else {
                    cEnds[i].updateUpperBound(last.getUB(), aCause);
                }
                if (cEnds[i].isInstantiated()) idempotent = false;

            }
        }
        return idempotent;
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
