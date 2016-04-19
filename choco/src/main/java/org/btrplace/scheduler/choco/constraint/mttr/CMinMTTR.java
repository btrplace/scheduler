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

import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.MinMTTR;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.SliceUtils;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.Transition;
import org.btrplace.scheduler.choco.transition.TransitionUtils;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import java.util.*;

/**
 * An objective that minimizes the time to repair a non-viable model.
 *
 * @author Fabien Hermenier
 */
public class CMinMTTR implements org.btrplace.scheduler.choco.constraint.CObjective {

    private List<Constraint> costConstraints;

    private boolean costActivated = false;

    private ReconfigurationProblem rp;

    /**
     * Make a new objective.
     */
    public CMinMTTR(MinMTTR m) {
        costConstraints = new ArrayList<>();
    }

    public CMinMTTR() {
        this(null);
    }

    @Override
    public boolean inject(Parameters ps, ReconfigurationProblem p) throws SchedulerException {
        this.rp = p;
        costActivated = false;
        List<IntVar> mttrs = new ArrayList<>();
        for (Transition m : p.getVMActions()) {
            mttrs.add(m.getEnd());
        }
        for (Transition m : p.getNodeActions()) {
            mttrs.add(m.getEnd());
        }
        IntVar[] costs = mttrs.toArray(new IntVar[mttrs.size()]);
        Solver s = p.getSolver();
        IntVar cost = VariableFactory.bounded(p.makeVarLabel("globalCost"), 0, Integer.MAX_VALUE / 100, s);

        Constraint costConstraint = IntConstraintFactory.sum(costs, cost);
        costConstraints.clear();
        costConstraints.add(costConstraint);

        p.setObjective(true, cost);

        injectPlacementHeuristic(p, ps, cost);
        return true;
    }

    private void injectPlacementHeuristic(ReconfigurationProblem p, Parameters ps, IntVar cost) {

        Model mo = p.getSourceModel();
        Mapping map = mo.getMapping();

        OnStableNodeFirst schedHeuristic = new OnStableNodeFirst(p);

        //Get the VMs to place
        Set<VM> onBadNodes = new HashSet<>(p.getManageableVMs());

        //Get the VMs that runs and have a pretty low chances to move
        Set<VM> onGoodNodes = map.getRunningVMs(map.getOnlineNodes());
        onGoodNodes.removeAll(onBadNodes);

        List<VMTransition> goodActions = p.getVMActions(onGoodNodes);
        List<VMTransition> badActions = p.getVMActions(onBadNodes);

        Solver s = p.getSolver();

        //Get the VMs to move for exclusion issue
        Set<VM> vmsToExclude = new HashSet<>(p.getManageableVMs());
        for (Iterator<VM> ite = vmsToExclude.iterator(); ite.hasNext(); ) {
            VM vm = ite.next();
            if (!(map.isRunning(vm) && p.getFutureRunningVMs().contains(vm))) {
                ite.remove();
            }
        }
        List<AbstractStrategy> strategies = new ArrayList<>();

        Map<IntVar, VM> pla = VMPlacementUtils.makePlacementMap(p);
        if (!vmsToExclude.isEmpty()) {
            List<VMTransition> actions = new LinkedList<>();
            //Get all the involved slices
            for (VM vm : vmsToExclude) {
                if (p.getFutureRunningVMs().contains(vm)) {
                    actions.add(p.getVMAction(vm));
                }
            }
            IntVar[] scopes = SliceUtils.extractHoster(TransitionUtils.getDSlices(actions));

            strategies.add(new IntStrategy(scopes, new MovingVMs(p, map, actions), new RandomVMPlacement(p, pla, true, ps.getRandomSeed())));
        }

        placeVMs(ps, strategies, badActions, schedHeuristic, pla);
        placeVMs(ps, strategies, goodActions, schedHeuristic, pla);

        //Reinstantations. Try to reinstantiate first
        List<IntVar> migs = new ArrayList<>();
        for (VMTransition t : rp.getVMActions()) {
            if (t instanceof RelocatableVM) {
                migs.add(((RelocatableVM) t).getRelocationMethod());
            }
        }
        strategies.add(ISF.custom(new MyInputOrder<>(s), ISF.max_value_selector(), migs.toArray(new IntVar[migs.size()])));

        if (!p.getNodeActions().isEmpty()) {
            //Boot some nodes if needed
            strategies.add(new IntStrategy(TransitionUtils.getStarts(p.getNodeActions()), new MyInputOrder<>(s), new IntDomainMin()));
        }

        ///SCHEDULING PROBLEM
        MovementGraph gr = new MovementGraph(rp);
        strategies.add(new IntStrategy(SliceUtils.extractStarts(TransitionUtils.getDSlices(rp.getVMActions())), new StartOnLeafNodes(rp, gr), new IntDomainMin()));
        strategies.add(new IntStrategy(schedHeuristic.getScope(), schedHeuristic, new IntDomainMin()));

        strategies.add(ISF.custom(new MyInputOrder<>(s), ISF.min_value_selector(), TransitionUtils.getEnds(rp.getVMActions())));
        //At this stage only it matters to plug the cost constraints
        strategies.add(new IntStrategy(new IntVar[]{p.getEnd(), cost}, new MyInputOrder<>(s, this), new IntDomainMin()));

        s.getSearchLoop().set(new StrategiesSequencer(s.getEnvironment(), strategies.toArray(new AbstractStrategy[strategies.size()])));
    }

    /*
     * Try to place the VMs associated on the actions in a random node while trying first to stay on the current node
     */
    private void placeVMs(Parameters ps, List<AbstractStrategy> strategies, List<VMTransition> actions, OnStableNodeFirst schedHeuristic, Map<IntVar, VM> map) {
        IntValueSelector rnd = new RandomVMPlacement(rp, map, true, ps.getRandomSeed());
        if (!actions.isEmpty()) {
            IntVar[] hosts = SliceUtils.extractHoster(TransitionUtils.getDSlices(actions));
            if (hosts.length > 0) {
                strategies.add(new IntStrategy(hosts, new HostingVariableSelector(schedHeuristic), rnd));
            }
        }
    }

    @Override
    public Set<VM> getMisPlacedVMs(Instance i) {
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
            costConstraints.forEach(s::post);
        }
    }

    
    @Override
    public String toString() {
        return "minimizeMTTR()";
    }
}
