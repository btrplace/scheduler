/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.solver.choco.constraint.minMTTR;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.constraint.Constraint;
import btrplace.model.constraint.MinMTTR;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.actionModel.ActionModel;
import btrplace.solver.choco.actionModel.ActionModelUtils;
import btrplace.solver.choco.actionModel.VMActionModel;
import btrplace.solver.choco.constraint.ChocoConstraint;
import btrplace.solver.choco.constraint.ChocoConstraintBuilder;
import choco.Choco;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.Constant;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.ResolutionPolicy;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.*;

/**
 * An objective that minimizes the time to repair a non-viable model.
 *
 * @author Fabien Hermenier
 */
public class CMinMTTR implements ChocoConstraint {

    private List<SConstraint> costConstraints;

    private boolean costActivated = false;

    private ReconfigurationProblem rp;

    /**
     * Make a new objective.
     */
    public CMinMTTR() {
        costConstraints = new ArrayList<>();
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        this.rp = rp;
        costActivated = false;
        List<IntDomainVar> mttrs = new ArrayList<>();
        for (ActionModel m : rp.getVMActions()) {
            mttrs.add(m.getEnd());
        }
        for (ActionModel m : rp.getNodeActions()) {
            mttrs.add(m.getEnd());
        }
        IntDomainVar[] costs = mttrs.toArray(new IntDomainVar[mttrs.size()]);
        CPSolver s = rp.getSolver();
        IntDomainVar cost = s.createBoundIntVar(rp.makeVarLabel("globalCost"), 0, Choco.MAX_UPPER_BOUND);

        SConstraint costConstraint = s.eq(cost, CPSolver.sum(costs));
        costConstraints.clear();
        costConstraints.add(costConstraint);

        s.getConfiguration().putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MINIMIZE);
        s.setObjective(cost);
        //We set a restart limit by default, this may be useful especially with very small infrastructure
        //as the risk of cyclic dependencies increase and their is no solution for the moment to detect cycle
        //in the scheduling part
        //Restart limit = 2 * number of VMs in the DC.
        if (rp.getVMs().length > 0) {
            s.setGeometricRestart(rp.getVMs().length * 2, 1.5d);
            s.setRestart(true);
        }
        injectPlacementHeuristic(rp, cost);
        return true;
    }

    private void injectPlacementHeuristic(ReconfigurationProblem rp, IntDomainVar cost) {

        Model mo = rp.getSourceModel();
        Mapping map = mo.getMapping();

        List<ActionModel> actions = new ArrayList<>();
        Collections.addAll(actions, rp.getVMActions());
        OnStableNodeFirst schedHeuristic = new OnStableNodeFirst("stableNodeFirst", rp, actions, this);

        //Get the VMs to move
        Set<VM> onBadNodes = rp.getManageableVMs();

        for (VM vm : map.getSleepingVMs()) {
            if (rp.getFutureRunningVMs().contains(vm)) {
                onBadNodes.add(vm);
            }
        }

        Set<VM> onGoodNodes = new HashSet<>(map.getRunningVMs());
        onGoodNodes.removeAll(onBadNodes);

        List<VMActionModel> goodActions = new ArrayList<>();
        for (VM vm : onGoodNodes) {
            goodActions.add(rp.getVMAction(vm));
        }
        List<VMActionModel> badActions = new ArrayList<>();
        for (VM vm : onBadNodes) {
            badActions.add(rp.getVMAction(vm));
        }

        CPSolver s = rp.getSolver();

        //Get the VMs to move for exclusion issue
        Set<VM> vmsToExclude = new HashSet<>(rp.getManageableVMs());
        for (Iterator<VM> ite = vmsToExclude.iterator(); ite.hasNext(); ) {
            VM vm = ite.next();
            if (!(map.getRunningVMs().contains(vm) && rp.getFutureRunningVMs().contains(vm))) {
                ite.remove();
            }
        }
        Map<IntDomainVar, VM> pla = VMPlacementUtils.makePlacementMap(rp);

        s.addGoal(new AssignVar(new MovingVMs("movingVMs", rp, map, vmsToExclude), new RandomVMPlacement("movingVMs", rp, pla, true)));
        HostingVariableSelector selectForBads = new HostingVariableSelector("selectForBads", rp, ActionModelUtils.getDSlices(badActions), schedHeuristic);
        s.addGoal(new AssignVar(selectForBads, new RandomVMPlacement("selectForBads", rp, pla, true)));


        HostingVariableSelector selectForGoods = new HostingVariableSelector("selectForGoods", rp, ActionModelUtils.getDSlices(goodActions), schedHeuristic);
        s.addGoal(new AssignVar(selectForGoods, new RandomVMPlacement("selectForGoods", rp, pla, true)));

        //VMs to run
        Set<VM> vmsToRun = new HashSet<>(map.getReadyVMs());
        vmsToRun.removeAll(rp.getFutureReadyVMs());

        VMActionModel[] runActions = new VMActionModel[vmsToRun.size()];
        int i = 0;
        for (VM vm : vmsToRun) {
            runActions[i++] = rp.getVMAction(vm);
        }
        HostingVariableSelector selectForRuns = new HostingVariableSelector("selectForRuns", rp, ActionModelUtils.getDSlices(runActions), schedHeuristic);
        s.addGoal(new AssignVar(selectForRuns, new RandomVMPlacement("selectForRuns", rp, pla, true)));

        s.addGoal(new AssignVar(new StartingNodes("startingNodes", rp, rp.getNodeActions()), new MinVal()));
        ///SCHEDULING PROBLEM
        s.addGoal(new AssignOrForbidIntVarVal(schedHeuristic, new MinVal()));

        //At this stage only it matters to plug the cost constraints
        s.addGoal(new AssignVar(new StaticVarOrder(rp.getSolver(), new IntDomainVar[]{rp.getEnd(), cost}), new MinVal()));
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }

    /**
     * Post the constraints related to the objective.
     */
    public void postCostConstraints() {
        if (!costActivated) {
            rp.getLogger().debug("Post the cost-oriented constraints");
            costActivated = true;
            CPSolver s = rp.getSolver();
            for (SConstraint c : costConstraints) {
                s.postCut(c);
            }
            try {
                s.propagate();
            } catch (ContradictionException e) {
                s.setFeasible(false);
                s.post(Constant.FALSE);
            }
        }
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends Constraint> getKey() {
            return MinMTTR.class;
        }

        @Override
        public CMinMTTR build(Constraint cstr) {
            return new CMinMTTR();
        }
    }
}
