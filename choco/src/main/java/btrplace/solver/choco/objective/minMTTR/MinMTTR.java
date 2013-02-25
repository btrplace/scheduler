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

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.solver.SolverException;
import btrplace.solver.choco.*;
import btrplace.solver.choco.actionModel.RelocatableVMModel;
import btrplace.solver.choco.actionModel.ResumeVMModel;
import choco.Choco;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ResolutionPolicy;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntHashSet;
import gnu.trove.TLongIntHashMap;

import java.util.*;

/**
 * An objective that minimize the time to repair a non-viable model.
 *
 * @author Fabien Hermenier
 */
public class MinMTTR implements ReconfigurationObjective {

    public MinMTTR() {

    }

    @Override
    public void inject(ReconfigurationProblem rp) throws SolverException {
        List<IntDomainVar> mttrs = new ArrayList<IntDomainVar>();
        for (ActionModel m : rp.getVMActions()) {
            mttrs.add(m.getEnd());
        }
        for (ActionModel m : rp.getNodeActions()) {
            mttrs.add(m.getEnd());
        }
        IntDomainVar[] costs = mttrs.toArray(new IntDomainVar[mttrs.size()]);
        CPSolver s = rp.getSolver();
        IntDomainVar cost = s.createBoundIntVar(rp.makeVarLabel("globalCost"), 0, Choco.MAX_UPPER_BOUND);
        s.post(s.eq(cost, CPSolver.sum(costs)));


        s.getConfiguration().putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MINIMIZE);
        s.setObjective(cost);

        injectPlacementHeuristic(rp, new HashSet<UUID>(), cost);
    }

    private void injectPlacementHeuristic(ReconfigurationProblem rp, Set<UUID> managedVMs, IntDomainVar cost) {

        Model mo = rp.getSourceModel();
        Mapping map = mo.getMapping();


        //Compute the nodes that will not leave resources. Awesome candidates to place VMs
        //on as they will be scheduled asap.
        TIntHashSet[] favorites;
        if (!managedVMs.isEmpty()) {

            favorites = new TIntHashSet[2];
            favorites[0] = new TIntHashSet();
            favorites[1] = new TIntHashSet();

            //Composed with nodes that do not host misplaced VMs.
            Set<UUID> involded = new HashSet<UUID>(map.getAllNodes());
            for (UUID n : involded) {
                favorites[0].add(rp.getNode(n));
            }
            for (UUID vm : managedVMs) {
                UUID n = map.getVMLocation(vm);
                if (n != null && involded.remove(n)) {
                    int i = rp.getNode(n);
                    favorites[0].remove(i);
                    favorites[1].add(i);
                }
            }
            //Then remove nodes that have VMs that must be suspended or terminated
            for (UUID vm : rp.getFutureRunningVMs()) {
                if (map.getRunningVMs().contains(vm)) {
                    UUID n = map.getVMLocation(vm);
                    int i = rp.getNode(n);
                    if (n != null && involded.remove(n)) {
                        favorites[1].add(i);
                        favorites[0].remove(i);
                    }
                }
                //Don't care about sleeping that stay sleeping
            }
            for (UUID vm : rp.getFutureReadyVMs()) {
                UUID n = map.getVMLocation(vm);
                int i = rp.getNode(n);
                if (involded.remove(n)) {
                    favorites[1].add(i);
                    favorites[0].remove(i);
                }
            }
        } else {
            favorites = new TIntHashSet[1];
            favorites[0] = new TIntHashSet();
            for (UUID n : rp.getNodes()) {
                favorites[0].add(rp.getNode(n));
            }
        }


        //Get the VMs to move
        Set<UUID> onBadNodes = new HashSet<UUID>();

        for (UUID vm : map.getSleepingVMs()) {
            if (rp.getFutureRunningVMs().contains(vm)) {
                onBadNodes.add(vm);
            }
        }

        Set<UUID> onGoodNodes = new HashSet<UUID>(map.getRunningVMs());
        onGoodNodes.removeAll(onBadNodes);

        //TODO: sorting stuff
        //Collections.sort(onGoodNodes, dsc);
        //Collections.sort(onBadNodes, dsc);

        List<VMActionModel> goodActions = new ArrayList<VMActionModel>();
        for (UUID vm : onGoodNodes) {
            goodActions.add(rp.getVMAction(vm));
        }
        List<VMActionModel> badActions = new ArrayList<VMActionModel>();
        for (UUID vm : onBadNodes) {
            badActions.add(rp.getVMAction(vm));
        }

        Set<UUID> relocalisables = rp.getFutureRunningVMs();
        TLongIntHashMap oldLocation = new TLongIntHashMap(relocalisables.size());

        CPSolver s = rp.getSolver();
        for (UUID vm : relocalisables) {
            int idx = rp.getVM(vm);
            VMActionModel a = rp.getVMAction(vm);
            if (a.getClass() == RelocatableVMModel.class || a.getClass() == ResumeVMModel.class) {
                oldLocation.put(a.getDSlice().getHoster().getIndex(), rp.getCurrentVMLocation(idx));
            }
        }


        //Get the VMs to move for exclusion issue
        Set<UUID> vmsToExlude = new HashSet<UUID>(map.getAllVMs());
        //TODO: sorting stuff
        //Collections.sort(vmsToExlude, dsc);
        s.addGoal(new AssignVar(new MovingVMs(rp, map, vmsToExlude), new AvoidVMRelocation(rp, oldLocation, favorites, AvoidVMRelocation.RelocationHeuristic.worstFit)));


        HostingVariableSelector selectForBads = new HostingVariableSelector(rp, ActionModelUtils.getDSlices(badActions));
        s.addGoal(new AssignVar(selectForBads, new AvoidVMRelocation(rp, oldLocation, favorites, AvoidVMRelocation.RelocationHeuristic.worstFit)));


        HostingVariableSelector selectForGoods = new HostingVariableSelector(rp, ActionModelUtils.getDSlices(goodActions));
        s.addGoal(new AssignVar(selectForGoods, new AvoidVMRelocation(rp, oldLocation, favorites, AvoidVMRelocation.RelocationHeuristic.worstFit)));

        //VMs to run
        Set<UUID> vmsToRun = new HashSet<UUID>(map.getReadyVMs());
        vmsToRun.removeAll(rp.getFutureReadyVMs());

        VMActionModel[] runActions = new VMActionModel[vmsToRun.size()];
        int i = 0;
        for (UUID vm : vmsToRun) {
            runActions[i++] = rp.getVMAction(vm);
        }
        HostingVariableSelector selectForRuns = new HostingVariableSelector(rp, ActionModelUtils.getDSlices(runActions));


        s.addGoal(new AssignVar(selectForRuns, new AvoidVMRelocation(rp, oldLocation, favorites, AvoidVMRelocation.RelocationHeuristic.worstFit)));

        ///SCHEDULING PROBLEM
        List<ActionModel> actions = new ArrayList<ActionModel>();
        Collections.addAll(actions, rp.getVMActions());
        s.addGoal(new AssignOrForbidIntVarVal(new PureIncomingFirst2(rp, actions), new MinVal()));

        s.addGoal(new AssignVar(new StaticVarOrder(rp.getSolver(), new IntDomainVar[]{rp.getEnd(), cost}), new MinVal()));
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }
}
