/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.constraint.mttr;


import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableValueIterator;

import java.util.Arrays;
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

  private final boolean stay;

  private final ReconfigurationProblem rp;

  private final Random rnd;

  private final Map<IntVar, VM> vmPlacement;

    private TIntHashSet[] ranks;

  private final int[] nodeMap;

  private final VMTransition[] actionMap;

    /**
     * Make a new heuristic.
     *
     * @param p           the problem to rely on
     * @param pVarMapping a map to indicate the VM associated to each of the placement variable
     * @param stayFirst   {@code true} to force an already VM to stay on its current node if possible
     * @param seed the seed to use to initialize the random number generator
     */
    public RandomVMPlacement(ReconfigurationProblem p, Map<IntVar, VM> pVarMapping, boolean stayFirst, long seed) {
        this(p, pVarMapping, null, stayFirst, seed);
    }

    /**
     * Make a new heuristic.
     *
     * @param p           the problem to rely on
     * @param pVarMapping a map to indicate the VM associated to each of the placement variable
     * @param priorities  a list of favorites servers. Servers in rank i will be favored wrt. servers in rank i + 1
     * @param stayFirst   {@code true} to force an already VM to stay on its current node if possible
     * @param seed the seed to use to initialize the random number generator
     */
    public RandomVMPlacement(ReconfigurationProblem p, Map<IntVar, VM> pVarMapping, TIntHashSet[] priorities, boolean stayFirst, long seed) {
        stay = stayFirst;
        this.rp = p;
        rnd = new Random(seed);
        vmPlacement = pVarMapping;
        if (priorities != null) {
            this.ranks = Arrays.copyOf(priorities, priorities.length);
        }

        int maxId = 0;
        for (Node n : p.getNodes()) {
            if (maxId < n.id()) {
                maxId = n.id();
            }
        }

        nodeMap = new int[maxId + 1];
        for (Node n : p.getNodes()) {
            nodeMap[n.id()] = rp.getNode(n);
        }

        maxId = 0;
        for (VM v : p.getFutureRunningVMs()) {
            int idx = v.id();
            if (maxId < idx) {
                maxId = idx;
            }
        }
        actionMap = new VMTransition[maxId + 1];
        for (VM v : p.getFutureRunningVMs()) {
            actionMap[v.id()] = p.getVMAction(v);
        }

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

    /**
     * Check if a VM can stay on its current node.
     *
     * @param rp the reconfiguration problem.
     * @param vm the VM
     * @return {@code true} iff the VM can stay
     */
    public int canStay(ReconfigurationProblem rp, VM vm) {
        Mapping m = rp.getSourceModel().getMapping();
        if (m.isRunning(vm)) {
            Node n = m.getVMLocation(vm);
            int curPos = nodeMap[n.id()];
            if (actionMap[vm.id()].getDSlice().getHoster().contains(curPos)) {
                return curPos;
            }
        }
        return -1;
    }

    @Override
    public int selectValue(IntVar x) {
        if (stay) {
            VM vm = vmPlacement.get(x);
            int nIdx = canStay(rp, vm);
            if (nIdx >= 0) {
                Node n = rp.getSourceModel().getMapping().getVMLocation(vm);
                return nodeMap[n.id()];
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
