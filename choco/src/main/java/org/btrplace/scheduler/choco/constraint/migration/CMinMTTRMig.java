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

package org.btrplace.scheduler.choco.constraint.migration;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.migration.MinMTTRMig;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.SliceUtils;
import org.btrplace.scheduler.choco.constraint.ChocoConstraintBuilder;
import org.btrplace.scheduler.choco.constraint.mttr.*;
import org.btrplace.scheduler.choco.transition.ShutdownableNode;
import org.btrplace.scheduler.choco.transition.Transition;
import org.btrplace.scheduler.choco.transition.TransitionUtils;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import java.util.*;

/**
 * An objective that minimizes the time to repair a non-viable model involving a set of migrations.
 *
 * @author Vincent Kherbache
 */
public class CMinMTTRMig implements org.btrplace.scheduler.choco.constraint.CObjective {

    private ReconfigurationProblem rp;
    private List<Constraint> costConstraints;
    private boolean costActivated = false;

    /**
     * Make a new Objective.
     */
    public CMinMTTRMig() { costConstraints = new ArrayList<>(); }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {

        this.rp = rp;
        List<IntVar> endVars = new ArrayList<IntVar>();

        // Define the cost constraint: sum of all actions' end time
        for (Transition m : rp.getVMActions()) { endVars.add(m.getEnd()); }
        for (Transition m : rp.getNodeActions()) { endVars.add(m.getEnd()); }
        IntVar[] costs = endVars.toArray(new IntVar[endVars.size()]);
        IntVar cost = VariableFactory.bounded(rp.makeVarLabel("costEndVars"), 0, Integer.MAX_VALUE/100, rp.getSolver());
        costConstraints.add(IntConstraintFactory.sum(costs, cost));

        // Set the objective, minimize the cost
        rp.setObjective(true, cost);

        // TODO: inject placement heuristic if dst nodes are not yet known (1st pass) ?
        //injectPlacementHeuristic(cost);

        // Inject the scheduling heuristic
        injectSchedulingHeuristic(cost);

        // Post the cost constraint
        postCostConstraints();

        return true;
    }

    /**
     * Inject a specific scheduling heuristic to the solver.
     *
     * @param cost  the global cost variable.
     */
    private void injectSchedulingHeuristic(IntVar cost) {

        // Init a list of strategies
        List<AbstractStrategy> strategies = new ArrayList<>();

        // Init a list of vars
        List<IntVar> endVars = new ArrayList<IntVar>();

        // Per node to decommission (Boot dst node -> Migrate all VMs -> Shutdown src node) strategy
        for (Node n : rp.getNodes()) {
            endVars.clear();

            if (rp.getNodeAction(n) instanceof ShutdownableNode) {

                for (VMTransition a : rp.getVMActions()) {

                    if (rp.getNode(n) == (a.getCSlice().getHoster().getValue())) {

                        // Boot dst node (if known)
                        if (a.getDSlice().getHoster().isInstantiated()) {
                            if (!endVars.contains(rp.getNodeAction(rp.getNode(a.getDSlice().getHoster().getValue())).getEnd())) {
                                endVars.add(rp.getNodeAction(rp.getNode(a.getDSlice().getHoster().getValue())).getEnd());
                            }
                        }

                        // Migrate all
                        endVars.add(a.getEnd());
                    }
                }

                // Shutdown src node
                endVars.add(rp.getNodeAction(n).getEnd());
            }

            if (!endVars.isEmpty()) {
                //endVars.add(rp.getNodeAction(n).getHostingEnd());
                /*strategies.add(ISF.custom(
                        ISF.maxDomainSize_var_selector(),
                        ISF.mid_value_selector(),//.max_value_selector(),
                        ISF.split(), // Split from max
                        endVars.toArray(new IntVar[endVars.size()])
                ));*/
                strategies.add(ISF.minDom_LB(endVars.toArray(new IntVar[endVars.size()])));
            }
        }


        // Per decommissioning per link
        /*endVars.clear();
        CNetworkView cnv = (CNetworkView) rp.getView(CNetworkView.VIEW_ID);
        if (cnv == null) {
            throw new SchedulerException(rp.getSourceModel(), "Solver View '" + CNetworkView.VIEW_ID +
                    "' is required but missing");
        }
        List<List<MigrateVMTransition>> tasksPerLink = cnv.getMigrationsPerLink();
        if (!tasksPerLink.isEmpty()) {
            Collections.sort(tasksPerLink, (tasks, tasks2) -> tasks2.size() - tasks.size());
            for (List<MigrateVMTransition> migrations : tasksPerLink) {
                if (!migrations.isEmpty()) {
                    endVars.clear();

                    for (MigrateVMTransition m : migrations) {
                        endVars.add(m.getEnd());

                        Node src = map.getVMLocation(m.getVM());
                        Node dst = rp.getNode(m.getDSlice().getHoster().getValue());

                    }
                    strategies.add(ISF.custom(
                            ISF.minDomainSize_var_selector(),
                            ISF.mid_value_selector(),//.max_value_selector(),
                            ISF.split(), // Split from max
                            endVars.toArray(new IntVar[endVars.size()])
                    ));
                }
            }
        }*/

        /* End vars for all Nodes actions
        endVars.clear();
        for (NodeTransition a : rp.getNodeActions()) {
            endVars.add(a.getEnd());
        }
        if (!endVars.isEmpty()) {
            strategies.add(ISF.custom(
                    ISF.maxDomainSize_var_selector(),
                    ISF.mid_value_selector(),//.max_value_selector(),
                    ISF.split(), // Split from max
                    endVars.toArray(new IntVar[endVars.size()])
            ));
        }*/

        /* End vars for all Nodes shutdown actions
        endVars.clear();
        for (NodeTransition a : rp.getNodeActions()) {
            if (a instanceof ShutdownableNode) {
                endVars.add(a.getHostingEnd());
            }
        }
        if (!endVars.isEmpty()) {
            /*strategies.add(ISF.custom(
                    ISF.maxDomainSize_var_selector(),
                    ISF.mid_value_selector(),//.max_value_selector(),
                    ISF.split(), // Split from max
                    endVars.toArray(new IntVar[endVars.size()])
            ));*//*
            strategies.add(ISF.maxDom_Split(endVars.toArray(new IntVar[endVars.size()])));
        }*/

        /* End vars for all Nodes boot actions
        endVars.clear();
        for (NodeTransition a : rp.getNodeActions()) {
            if (a instanceof BootableNode) {
                endVars.add(a.getHostingStart());
            }
        }
        if (!endVars.isEmpty()) {
            /*strategies.add(ISF.custom(
                    ISF.maxDomainSize_var_selector(),
                    ISF.mid_value_selector(),//.max_value_selector(),
                    ISF.split(), // Split from max
                    endVars.toArray(new IntVar[endVars.size()])
            ));*//*
            strategies.add(ISF.maxDom_Split(endVars.toArray(new IntVar[endVars.size()])));
        }*/

        // End vars for all VMs actions
        /*endVars.clear();
        for (VMTransition a : rp.getVMActions()) {
            endVars.add(a.getEnd());
        }
        if (!endVars.isEmpty()) {
            /*strategies.add(ISF.custom(
                    ISF.maxDomainSize_var_selector(),
                    ISF.mid_value_selector(),//.max_value_selector(),
                    ISF.split(), // Split from max
                    endVars.toArray(new IntVar[endVars.size()])
            ));*//*
            strategies.add(ISF.maxDom_Split(endVars.toArray(new IntVar[endVars.size()])));
        }*/

        /* End vars for all actions
        endVars.clear();
        for (Transition m : rp.getVMActions()) { endVars.add(m.getEnd()); }
        for (Transition m : rp.getNodeActions()) { endVars.add(m.getEnd()); }
        endVars.add(rp.getEnd());
        strategies.add(ISF.custom(
                ISF.minDomainSize_var_selector(),
                ISF.mid_value_selector(),//.max_value_selector(),
                ISF.split(), // Split from max
                endVars.toArray(new IntVar[endVars.size()])
        ));*/
        //strategies.add(ISF.maxDom_Split(endVars.toArray(new IntVar[endVars.size()])));


        /* End vars for all VMs actions PER NODE
        for (Node n : rp.getNodes()) {
            endVars.clear();
            for (VMTransition a : rp.getVMActions()) {
                if (rp.getNode(n) == (a.getCSlice().getHoster().getValue())) {
                    endVars.add(a.getEnd());
                }
            }
            if (!endVars.isEmpty()) {
                //endVars.add(rp.getNodeAction(n).getHostingEnd());
                strategies.add(ISF.custom(
                        ISF.minDomainSize_var_selector(),
                        ISF.mid_value_selector(),//.max_value_selector(),
                        ISF.split(), // Split from max
                        endVars.toArray(new IntVar[endVars.size()])
                ));
            }
        }*/

        /* Add strategy for the cost constraint
        strategies.add(ISF.custom(
                ISF.minDomainSize_var_selector(),
                ISF.mid_value_selector(), //.max_value_selector(),
                ISF.split(), // Split from max
                new IntVar[]{rp.getEnd()}
        ));*/


        // Set the strategies in the added order
        strategies.add(new IntStrategy(new IntVar[]{rp.getEnd(), cost}, new InputOrder<>(), new IntDomainMin()));

        // Add all defined strategies
        rp.getSolver().getSearchLoop().set(
                new StrategiesSequencer(
                        rp.getSolver().getEnvironment(),
                        strategies.toArray(new AbstractStrategy[strategies.size()])
                )
        );
        //s.set(strategies.toArray(new AbstractStrategy[strategies.size()]));
    }

    private void injectPlacementHeuristic(IntVar cost) {

        Model mo = rp.getSourceModel();
        Mapping map = mo.getMapping();

        OnStableNodeFirst schedHeuristic = new OnStableNodeFirst(rp, this);

        //Get the VMs to place
        Set<VM> onBadNodes = new HashSet<>(rp.getManageableVMs());

        //Get the VMs that runs and have a pretty low chances to move
        Set<VM> onGoodNodes = map.getRunningVMs(map.getOnlineNodes());
        onGoodNodes.removeAll(onBadNodes);

        VMTransition[] goodActions = rp.getVMActions(onGoodNodes);
        VMTransition[] badActions = rp.getVMActions(onBadNodes);

        Solver s = rp.getSolver();

        //Get the VMs to move for exclusion issue
        Set<VM> vmsToExclude = new HashSet<>(rp.getManageableVMs());
        for (Iterator<VM> ite = vmsToExclude.iterator(); ite.hasNext(); ) {
            VM vm = ite.next();
            if (!(map.isRunning(vm) && rp.getFutureRunningVMs().contains(vm))) {
                ite.remove();
            }
        }
        List<AbstractStrategy> strategies = new ArrayList<>();

        Map<IntVar, VM> pla = VMPlacementUtils.makePlacementMap(rp);
        if (!vmsToExclude.isEmpty()) {
            List<VMTransition> actions = new LinkedList<>();
            //Get all the involved slices
            for (VM vm : vmsToExclude) {
                if (rp.getFutureRunningVMs().contains(vm)) {
                    actions.add(rp.getVMAction(vm));
                }
            }
            IntVar[] scopes = SliceUtils.extractHoster(TransitionUtils.getDSlices(actions));

            strategies.add(new IntStrategy(scopes, new MovingVMs(rp, map, actions),
                    new RandomVMPlacement(rp, pla, true)));
        }

        placeVMs(strategies, badActions, schedHeuristic, pla);
        placeVMs(strategies, goodActions, schedHeuristic, pla);

        //VMs to run
/*        Set<VM> vmsToRun = new HashSet<>(map.getReadyVMs());
        vmsToRun.removeAll(p.getFutureReadyVMs());

        VMTransition[] runActions = p.getVMActions(vmsToRun);

        placeVMs(strategies, runActions, schedHeuristic, pla);
  */

        if (rp.getNodeActions().length > 0) {
            //Boot some nodes if needed
            strategies.add(new IntStrategy(TransitionUtils.getStarts(rp.getNodeActions()),
                    new InputOrder<>(), new IntDomainMin()));
        }

        ///SCHEDULING PROBLEM
        MovementGraph gr = new MovementGraph(rp);
        strategies.add(new IntStrategy(SliceUtils.extractStarts(TransitionUtils.getDSlices(rp.getVMActions())),
                new StartOnLeafNodes(rp, gr), new IntDomainMin()));
        strategies.add(new IntStrategy(schedHeuristic.getScope(), schedHeuristic, new IntDomainMin()));

        //At this stage only it matters to plug the cost constraints
        strategies.add(new IntStrategy(new IntVar[]{rp.getEnd(), cost}, new InputOrder<>(), new IntDomainMin()));

        s.getSearchLoop().set(new StrategiesSequencer(s.getEnvironment(),
                strategies.toArray(new AbstractStrategy[strategies.size()])));
    }

    /*
     * Try to place the VMs associated on the actions in a random node while trying first to stay on the current node
     */
    private void placeVMs(List<AbstractStrategy> strategies, VMTransition[] actions,
                          OnStableNodeFirst schedHeuristic, Map<IntVar, VM> map) {

        //IntValueSelector quart = new RandOverQuartilePlacement(rp, map, getComparator(), 1, true);
        IntValueSelector rnd = new RandomVMPlacement(rp, map, true);
        if (actions.length > 0) {
            IntVar[] hosts = SliceUtils.extractHoster(TransitionUtils.getDSlices(actions));
            if (hosts.length > 0) {
                strategies.add(new IntStrategy(hosts, new HostingVariableSelector(schedHeuristic), rnd));
            }
        }
    }

    @Override
    public void postCostConstraints() {
        //TODO: Delay insertion ?
        if (!costActivated) {
            rp.getLogger().debug("Post the cost-oriented constraints");
            costActivated = true;
            Solver s = rp.getSolver();
            for (Constraint c : costConstraints) {
                s.post(c);
            }
        }
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model m) {
        return Collections.emptySet();
    }


    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoConstraintBuilder {
        @Override
        public Class<? extends org.btrplace.model.constraint.Constraint> getKey() {
            return MinMTTRMig.class;
        }

        @Override
        public CMinMTTRMig build(org.btrplace.model.constraint.Constraint cstr) {
            return new CMinMTTRMig();
        }
    }
}