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
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.MinMTTR;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.SliceUtils;
import btrplace.solver.choco.actionModel.ActionModel;
import btrplace.solver.choco.actionModel.ActionModelUtils;
import btrplace.solver.choco.actionModel.VMActionModel;
import btrplace.solver.choco.constraint.ChocoConstraintBuilder;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.search.limits.BacktrackCounter;
import solver.search.loop.monitors.SMF;
import solver.search.strategy.selectors.values.InDomainMin;
import solver.search.strategy.selectors.variables.InputOrder;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.Assignment;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.*;

/**
 * An objective that minimizes the time to repair a non-viable model.
 *
 * @author Fabien Hermenier
 */
public class CMinMTTR implements btrplace.solver.choco.constraint.CObjective {

    private List<Constraint> costConstraints;

    private boolean costActivated = false;

    private ReconfigurationProblem rp;

    /**
     * Make a new objective.
     */
    public CMinMTTR() {
        costConstraints = new ArrayList<>();
    }

    @Override
    public boolean inject(ReconfigurationProblem p) throws SolverException {
        this.rp = p;
        costActivated = false;
        List<IntVar> mttrs = new ArrayList<>();
        for (ActionModel m : p.getVMActions()) {
            mttrs.add(m.getEnd());
        }
        for (ActionModel m : p.getNodeActions()) {
            mttrs.add(m.getEnd());
        }
        IntVar[] costs = mttrs.toArray(new IntVar[mttrs.size()]);
        Solver s = p.getSolver();
        IntVar cost = VariableFactory.bounded(p.makeVarLabel("globalCost"), 0, Integer.MAX_VALUE / 100, s);

        Constraint costConstraint = IntConstraintFactory.sum(costs, cost);
        costConstraints.clear();
        costConstraints.add(costConstraint);

        p.setObjective(true, cost);

        //We set a restart limit by default, this may be useful especially with very small infrastructure
        //as the risk of cyclic dependencies increase and their is no solution for the moment to detect cycle
        //in the scheduling part
        //Restart limit = 2 * number of VMs in the DC.
        if (p.getVMs().length > 0) {
            SMF.geometrical(s, 1, 1.5d, new BacktrackCounter(p.getVMs().length * 2), Integer.MAX_VALUE);
        }
        injectPlacementHeuristic(p, cost);
        postCostConstraints();
        return true;
    }

    private void injectPlacementHeuristic(ReconfigurationProblem p, IntVar cost) {

        Model mo = p.getSourceModel();
        Mapping map = mo.getMapping();

        OnStableNodeFirst schedHeuristic = new OnStableNodeFirst(p, this);

        //Get the VMs to move
        Set<VM> onBadNodes = p.getManageableVMs();

        for (VM vm : map.getSleepingVMs()) {
            if (p.getFutureRunningVMs().contains(vm)) {
                onBadNodes.add(vm);
            }
        }

        Set<VM> onGoodNodes = new HashSet<>();
        for (Node n : map.getOnlineNodes()) {
            onGoodNodes.addAll(map.getRunningVMs(n));
        }
        onGoodNodes.removeAll(onBadNodes);

        List<VMActionModel> goodActions = new ArrayList<>();
        for (VM vm : onGoodNodes) {
            goodActions.add(p.getVMAction(vm));
        }
        List<VMActionModel> badActions = new ArrayList<>();
        for (VM vm : onBadNodes) {
            badActions.add(p.getVMAction(vm));
        }

        Solver s = p.getSolver();

        //Get the VMs to move for exclusion issue
        Set<VM> vmsToExclude = new HashSet<>(p.getManageableVMs());
        for (Iterator<VM> ite = vmsToExclude.iterator(); ite.hasNext(); ) {
            VM vm = ite.next();
            if (!(map.isRunning(vm) && p.getFutureRunningVMs().contains(vm))) {
                ite.remove();
            }
        }
        List<AbstractStrategy> strats = new ArrayList<>();

        Map<IntVar, VM> pla = VMPlacementUtils.makePlacementMap(p);
        if (!vmsToExclude.isEmpty()) {
            strats.add(new Assignment(new MovingVMs(p, map, vmsToExclude), new RandomVMPlacement(p, pla, true)));
        }

        if (!badActions.isEmpty()) {
            IntVar[] hosts = SliceUtils.extractHoster(ActionModelUtils.getDSlices(badActions));
            if (hosts.length > 0) {
                HostingVariableSelector selectForBads = new HostingVariableSelector(hosts, schedHeuristic);
                strats.add(new Assignment(selectForBads, new RandomVMPlacement(p, pla, true)));
            }
        }

        if (!goodActions.isEmpty()) {
            IntVar[] hosts = SliceUtils.extractHoster(ActionModelUtils.getDSlices(goodActions));
            if (hosts.length > 0) {
                HostingVariableSelector selectForGoods = new HostingVariableSelector(hosts, schedHeuristic);
                strats.add(new Assignment(selectForGoods, new RandomVMPlacement(p, pla, true)));
            }
        }

        //VMs to run
        Set<VM> vmsToRun = new HashSet<>(map.getReadyVMs());
        vmsToRun.removeAll(p.getFutureReadyVMs());

        VMActionModel[] runActions = new VMActionModel[vmsToRun.size()];
        int i = 0;
        for (VM vm : vmsToRun) {
            runActions[i++] = p.getVMAction(vm);
        }

        if (runActions.length > 0) {
            IntVar[] hosts = SliceUtils.extractHoster(ActionModelUtils.getDSlices(runActions));
            if (hosts.length > 0) {
                HostingVariableSelector selectForRuns = new HostingVariableSelector(hosts, schedHeuristic);
                strats.add(new Assignment(selectForRuns, new RandomVMPlacement(p, pla, true)));
            }
        }

        if (p.getNodeActions().length > 0) {
            strats.add(new Assignment(new InputOrder<>(ActionModelUtils.getStarts(p.getNodeActions())), new InDomainMin()));
        }

        ///SCHEDULING PROBLEM
        MovementGraph gr = new MovementGraph(rp);
        strats.add(new Assignment(new StartOnLeafNodes(rp, gr), new InDomainMin()));
        strats.add(new Assignment(schedHeuristic, new InDomainMin()));

        //At this stage only it matters to plug the cost constraints
        strats.add(new Assignment(new InputOrder<>(new IntVar[]{p.getEnd(), cost}), new InDomainMin()));

        s.getSearchLoop().set(new StrategiesSequencer(s.getEnvironment(), strats.toArray(new AbstractStrategy[strats.size()])));
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }

    /**
     * Post the constraints related to the objective.
     */
    @Override
    public void postCostConstraints() {
        //TODO: Delay insertion
        if (!costActivated) {
            rp.getLogger().debug("Post the cost-oriented constraints");
            costActivated = true;
            Solver s = rp.getSolver();
            for (Constraint c : costConstraints) {
                s.post(c);
            }
            /*try {
                s.propagate();
            } catch (ContradictionException e) {
                s.setFeasible(ESat.FALSE);
                //s.setFeasible(false);
                s.post(IntConstraintFactory.FALSE(s));
            } */
        }
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends btrplace.model.constraint.Constraint> getKey() {
            return MinMTTR.class;
        }

        @Override
        public CMinMTTR build(btrplace.model.constraint.Constraint cstr) {
            return new CMinMTTR();
        }
    }
}
