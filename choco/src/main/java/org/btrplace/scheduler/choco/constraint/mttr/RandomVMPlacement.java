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

package org.btrplace.scheduler.choco.constraint.mttr;


import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import org.btrplace.model.VM;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableValueIterator;

import java.util.Map;
import java.util.Random;


/**
 * A heuristic to place a VM on a server picked up randomly.
 * It is possible to force the VMs to stay on its current node
 * if it is possible.
 *
 * @author Fabien Hermenier
 */
public class RandomVMPlacement implements IntValueSelector {

    private boolean stay;

    private ReconfigurationProblem rp;

    private Random rnd;

    private Map<IntVar, VM> vmPlacement;

    private TIntHashSet[] ranks;


    /**
     * Make a new heuristic.
     *
     * @param p           the problem to rely on
     * @param pVarMapping a map to indicate the VM associated to each of the placement variable
     * @param stayFirst   {@code true} to force an already VM to stay on its current node if possible
     */
    public RandomVMPlacement(ReconfigurationProblem p, Map<IntVar, VM> pVarMapping, boolean stayFirst) {
        this(p, pVarMapping, null, stayFirst);
    }

    /**
     * Make a new heuristic.
     *
     * @param p           the problem to rely on
     * @param pVarMapping a map to indicate the VM associated to each of the placement variable
     * @param priorities  a list of favorites servers. Servers in rank i will be favored wrt. servers in rank i + 1
     * @param stayFirst   {@code true} to force an already VM to stay on its current node if possible
     */
    public RandomVMPlacement(ReconfigurationProblem p, Map<IntVar, VM> pVarMapping, TIntHashSet[] priorities, boolean stayFirst) {
        stay = stayFirst;
        this.rp = p;
        rnd = new Random();
        vmPlacement = pVarMapping;
        this.ranks = priorities;
    }

    /**
     * Random value but that consider the rank of nodes.
     * So values are picked up from the first rank possible.
     */
    private int randomWithRankedValues(IntVar x) {
        TIntArrayList[] values = new TIntArrayList[ranks.length];

        DisposableValueIterator ite = x.getValueIterator(true);
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
    private int randomValue(IntVar x) {
        int i = rnd.nextInt(x.getDomainSize());
        DisposableValueIterator ite = x.getValueIterator(true);
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
    public int selectValue(IntVar x) {
        if (stay) {
            VM vm = vmPlacement.get(x);
            if (VMPlacementUtils.canStay(rp, vm)) {
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
            return nIdx;
        }
        return x.getValue();
    }
}
