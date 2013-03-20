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
import gnu.trove.list.array.TIntArrayList;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * A heuristic to place a VM on a server picked up randomly.
 * It is possible to force the VMs to stay on its current node
 * if it is possible.
 *
 * @author Fabien Hermenier
 */
public class RandomVMPlacement implements ValSelector<IntDomainVar> {

    private boolean stay;

    private ReconfigurationProblem rp;

    private Random rnd;

    private Map<IntDomainVar, UUID> vmPlacement;

    private TIntHashSet[] ranks;

    private String dbgLbl;

    /**
     * Make a new heuristic.
     *
     * @param dbgLbl      the debug label
     * @param rp          the problem to rely on
     * @param pVarMapping a map to indicate the VM associated to each of the placement variable
     * @param stayFirst   {@code true} to force an already VM to stay on its current node if possible
     */
    public RandomVMPlacement(String dbgLbl, ReconfigurationProblem rp, Map<IntDomainVar, UUID> pVarMapping, boolean stayFirst) {
        this(rp, pVarMapping, null, stayFirst);
        this.dbgLbl = dbgLbl;
    }

    /**
     * Make a new heuristic.
     *
     * @param rp          the problem to rely on
     * @param pVarMapping a map to indicate the VM associated to each of the placement variable
     * @param ranks       a list of favorites servers. Servers in rank i will be favored wrt. servers in rank i + 1
     * @param stayFirst   {@code true} to force an already VM to stay on its current node if possible
     */
    public RandomVMPlacement(ReconfigurationProblem rp, Map<IntDomainVar, UUID> pVarMapping, TIntHashSet[] ranks, boolean stayFirst) {
        stay = stayFirst;
        this.rp = rp;
        rnd = new Random();
        vmPlacement = pVarMapping;
        this.ranks = ranks;
    }

    /**
     * Random value but that consider the rank of nodes.
     * So values are picked up from the first rank possible.
     */
    private int randomWithRankedValues(IntDomainVar x) {
        TIntArrayList[] values = new TIntArrayList[ranks.length];

        DisposableIntIterator ite = x.getDomain().getIterator();
        try {
            while (ite.hasNext()) {
                int v = ite.next();
                int i;

                for (i = 0; i < ranks.length; i++) {
                    if (ranks[i].contains(v)) {
                        if (values[i] == null) {
                            values[i] = new TIntArrayList();
                        }
                        values[i].add(v);
                    }
                }
            }
        } finally {
            ite.dispose();
        }

        //We pick a random value in the first rank that is not empty (aka null here)
        for (TIntArrayList rank : values) {
            if (rank != null) {
                int v = rnd.nextInt(rank.size());
                return rank.get(v);
            }
        }
        return -1;
    }

    /**
     * Pick a random value inside the variable domain.
     */
    private int randomValue(IntDomainVar x) {
        int i = rnd.nextInt(x.getDomainSize());
        DisposableIntIterator ite = x.getDomain().getIterator();
        int pos = -1;
        try {
            while (i >= 0) {
                pos = ite.next();
                i--;
            }
        } finally {
            ite.dispose();
        }
        return pos;
    }

    @Override
    public int getBestVal(IntDomainVar x) {
        if (stay) {
            UUID vm = vmPlacement.get(x);
            if (VMPlacementUtils.canStay(rp, vm)) {
                rp.getLogger().debug("{} - VM {} stays on {} ({})", dbgLbl, vm, rp.getSourceModel().getMapping().getVMLocation(vm), rp.getNode(rp.getSourceModel().getMapping().getVMLocation(vm)));
                return rp.getNode(rp.getSourceModel().getMapping().getVMLocation(vm));
            }
        }

        if (!x.isInstantiated()) {
            int nIdx;
            if (ranks != null) {
                nIdx = randomWithRankedValues(x);
            } else {
                nIdx = randomValue(x);
            }
            rp.getLogger().debug("{} - VM {} move to {} ({})", dbgLbl, vmPlacement.get(x), rp.getNode(nIdx), nIdx);
            return nIdx;
        }
        rp.getLogger().debug("{} - {} ", dbgLbl, x);
        return x.getVal();
    }
}