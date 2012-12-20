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

package btrplace.solver.choco.objective.minMTTR;

import btrplace.solver.choco.ReconfigurationProblem;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntHashSet;
import gnu.trove.TLongIntHashMap;

/**
 * A value selector to try to place the VMs dslice to its current hosting
 * node when possible. If not, it considers the free space on the servers
 * to select one to try.
 *
 * @author Fabien Hermenier
 */
public class AvoidVMRelocation implements ValSelector<IntDomainVar> {
    public enum RelocationHeuristic {
        worstFit, bestFit, firstFit, random
    }

    private RelocationHeuristic opt;

    private TIntHashSet[] favorites;
    /**
     * The previous location of the running VMs.
     */
    private TLongIntHashMap oldLocation = new TLongIntHashMap();


    private ReconfigurationProblem rp;

    /**
     * Build a selector for a specific solver.
     *
     * @param s           the solver
     * @param oldLocation the current VMs location
     * @param favorites   an array of nodes RP identifier. First, try to place the VMs on the node that belong to the
     *                    set having the minimum array index
     * @param o           the indicator to indicate which node to try when the current node is not possible
     */
    public AvoidVMRelocation(ReconfigurationProblem s, TLongIntHashMap oldLocation, TIntHashSet[] favorites, RelocationHeuristic o) {
        this.opt = o;
        rp = s;
        this.favorites = favorites;

        this.oldLocation = oldLocation;
    }

    /**
     * Get the bin with the maximum remaining space.
     *
     * @param place the hoster variable of the slice to place.
     * @return {@code -1} if no host is available, otherwise the index of the node.
     */
    private int worstFit(IntDomainVar place) {
        DisposableIntIterator ite = place.getDomain().getIterator();
        int[] maxIdxs = new int[favorites.length];
        int[] maxVals = new int[favorites.length];

        //Initialization
        for (int i = 0; i < maxIdxs.length; i++) {
            maxIdxs[i] = -1;
            maxVals[i] = -1;
        }

        try {
            while (ite.hasNext()) {
                int bIdx = ite.next();
                //Get the group its belong to
                for (int i = 0; i < favorites.length; i++) {
                    if (favorites[i].contains(bIdx)) { //Got a candidate in group i
                        int bVal = 0;//dim == 0 ? pack.getRemainingCPU(bIdx) : pack.getRemainingMemory(bIdx);
                        if (bVal > maxVals[i]) {
                            maxVals[i] = bVal;
                            maxIdxs[i] = bIdx;
                        }
                    }
                }
            }
        } finally {
            ite.dispose();
        }
        for (int i = 0; i < maxVals.length; i++) {
            if (maxIdxs[i] >= 0) {
                //Plan.logger.debug("Choose value in group " + i);
                return maxIdxs[i];
            }
        }
        return -1;
    }

    /**
     * Get the bin with the minimum remaining space.
     *
     * @param place the hoster variable of the slice to place.
     * @return {@code -1} if no host is available, otherwise the index of the node.
     */
    private int bestFit(IntDomainVar place) {
        DisposableIntIterator ite = place.getDomain().getIterator();
        int[] minIdxs = new int[favorites.length];
        int[] minVals = new int[favorites.length];

        //Initialization
        for (int i = 0; i < minIdxs.length; i++) {
            minIdxs[i] = -1;
            minVals[i] = -1;
        }

        try {
            while (ite.hasNext()) {
                int bIdx = ite.next();
                //Get the group its belong to
                for (int i = 0; i < favorites.length; i++) {
                    if (favorites[i].contains(bIdx)) { //Got a candidate in group i
                        int bVal = 0;//dim == 0 ? pack.getRemainingCPU(bIdx) : pack.getRemainingMemory(bIdx);
                        if (bVal < minVals[i]) {
                            minVals[i] = bVal;
                            minIdxs[i] = bIdx;
                        }
                    }
                }
            }
        } finally {
            ite.dispose();
        }
        for (int i = 0; i < minIdxs.length; i++) {
            if (minIdxs[i] >= 0) {
                //Plan.logger.debug("Choose value in group " + i);
                return minIdxs[i];
            }
        }
        return -1;
    }

    private int random(IntDomainVar var) {
        return 0;  //To change body of created methods use File | Settings | File Templates.
    }


    private int firstFit(IntDomainVar host) {
        int minIdx = -1;
        int minVal = Integer.MAX_VALUE;

        for (int bIdx = host.getInf(); bIdx <= host.getSup(); bIdx = host.getDomain().getNextValue(bIdx)) {
            int bVal = 0;//dim == 0 ? pack.getRemainingCPU(bIdx) : pack.getRemainingMemory(bIdx);
            if (bVal < minVal) {
                minVal = bVal;
                minIdx = bIdx;
            }
        }
        return minIdx;
    }


    @Override
    public int getBestVal(IntDomainVar var) {
        int val = this.oldLocation.get(var.getIndex());
        if (this.oldLocation.containsKey(var.getIndex()) && var.canBeInstantiatedTo(val)) {
            //The VM can stay on the current node
            return val;
        }
        int to;
        //We choose a node depending on the heuristic
        switch (opt) {
            case worstFit:
                to = worstFit(var);
                break;
            case bestFit:
                to = bestFit(var);
                break;
            case firstFit:
                to = firstFit(var);
                break;
            case random:
                to = random(var);
            default:
                to = var.getInf();
        }
        return to;
    }
}
