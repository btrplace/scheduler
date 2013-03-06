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
import choco.Choco;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.solver.variables.integer.IntDomainVar;

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
/*
       s.post(s.eq(cost, CPSolver.sum(costs)));


        s.getConfiguration().putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MINIMIZE);
        s.setObjective(cost);
  */
        injectPlacementHeuristic(rp, cost);
    }

    private void injectPlacementHeuristic(ReconfigurationProblem rp, IntDomainVar cost) {

        Model mo = rp.getSourceModel();
        Mapping map = mo.getMapping();

        //Get the VMs to move
        Set<UUID> onBadNodes = new HashSet<UUID>();

        for (UUID vm : map.getSleepingVMs()) {
            if (rp.getFutureRunningVMs().contains(vm)) {
                onBadNodes.add(vm);
            }
        }

        Set<UUID> onGoodNodes = new HashSet<UUID>(map.getRunningVMs());
        onGoodNodes.removeAll(onBadNodes);

        List<VMActionModel> goodActions = new ArrayList<VMActionModel>();
        for (UUID vm : onGoodNodes) {
            goodActions.add(rp.getVMAction(vm));
        }
        List<VMActionModel> badActions = new ArrayList<VMActionModel>();
        for (UUID vm : onBadNodes) {
            badActions.add(rp.getVMAction(vm));
        }

        CPSolver s = rp.getSolver();

        //Get the VMs to move for exclusion issue
        Set<UUID> vmsToExclude = new HashSet<UUID>(rp.getManageableVMs());
        for (Iterator<UUID> ite = vmsToExclude.iterator(); ite.hasNext(); ) {
            UUID vm = ite.next();
            if (!(map.getRunningVMs().contains(vm) && rp.getFutureRunningVMs().contains(vm))) {
                ite.remove();
            }
        }
        Map<IntDomainVar, UUID> pla = VMPlacementUtils.makePlacementMap(rp);

        s.addGoal(new AssignVar(new MovingVMs(rp, map, vmsToExclude), new RandomVMPlacement(rp, pla, true)));

        HostingVariableSelector selectForBads = new HostingVariableSelector(rp, ActionModelUtils.getDSlices(badActions));
        s.addGoal(new AssignVar(selectForBads, new RandomVMPlacement(rp, pla, true)));


        HostingVariableSelector selectForGoods = new HostingVariableSelector(rp, ActionModelUtils.getDSlices(goodActions));
        s.addGoal(new AssignVar(selectForGoods, new RandomVMPlacement(rp, pla, true)));

        //VMs to run
        Set<UUID> vmsToRun = new HashSet<UUID>(map.getReadyVMs());
        vmsToRun.removeAll(rp.getFutureReadyVMs());

        VMActionModel[] runActions = new VMActionModel[vmsToRun.size()];
        int i = 0;
        for (UUID vm : vmsToRun) {
            runActions[i++] = rp.getVMAction(vm);
        }
        HostingVariableSelector selectForRuns = new HostingVariableSelector(rp, ActionModelUtils.getDSlices(runActions));


        s.addGoal(new AssignVar(selectForRuns, new RandomVMPlacement(rp, pla, true)));

        ///SCHEDULING PROBLEM
        List<ActionModel> actions = new ArrayList<ActionModel>();
        Collections.addAll(actions, rp.getVMActions());
        s.addGoal(new AssignOrForbidIntVarVal(new OnStableNodeFirst(rp, actions), new MinVal()));

        s.addGoal(new AssignVar(new StaticVarOrder(rp.getSolver(), new IntDomainVar[]{rp.getEnd(), cost}), new MinVal()));
    }

    @Override
    public Set<UUID> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }
}
