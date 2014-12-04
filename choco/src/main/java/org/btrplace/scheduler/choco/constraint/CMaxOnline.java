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

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.MaxOnline;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.view.CPowerView;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.VariableFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Choco implementation for the {@link MaxOnline} constraints.
 *
 * @author Tu Huynh Dang
 */
public class CMaxOnline implements ChocoConstraint {

    private final MaxOnline constraint;

    /**
     * Make a new constraint
     *
     * @param maxOn the constraint to rely on
     */
    public CMaxOnline(MaxOnline maxOn) {
        super();
        constraint = maxOn;
    }

    @Override
    public Set<VM> getMisPlacedVMs(Model model) {
        int on = 0;
        for (Node n : constraint.getInvolvedNodes()) {
            if (model.getMapping().isOnline(n)) {
                on++;
            }
        }
        if (on > constraint.getAmount()) {
            return model.getMapping().getRunningVMs(constraint.getInvolvedNodes());
        }
        return Collections.emptySet();
    }

    @Override
    public boolean inject(ReconfigurationProblem rp) throws SchedulerException {
        Solver solver = rp.getSolver();

        if (constraint.isContinuous()) {
            CPowerView view = (CPowerView) rp.getView(CPowerView.VIEW_ID);
            if (view == null) {
                view = new CPowerView(rp);
                if (!rp.addView(view)) {
                    throw new SchedulerException(rp.getSourceModel(), "Unable to attach view '" + CPowerView.VIEW_ID + "'");
                }
            }

            int numberOfTasks = constraint.getInvolvedNodes().size();
            int i = 0;
            int[] nodeIdx = new int[numberOfTasks];
            for (Node n : constraint.getInvolvedNodes()) {
                nodeIdx[i++] = rp.getNode(n);
            }
            IntVar capacity = VariableFactory.bounded("capacity", 0, constraint.getAmount(), solver);
            // The state of the node:
            IntVar[] heights = new IntVar[numberOfTasks];
            IntVar[] starts = new IntVar[numberOfTasks];
            IntVar[] ends = new IntVar[numberOfTasks];
            // Online duration:
            IntVar[] durations = new IntVar[numberOfTasks];
            // Online duration is modeled as a task
            Task[] taskVars = new Task[numberOfTasks];

            for (int idx = 0; idx < nodeIdx.length; idx++) {
                Node n = rp.getNode(nodeIdx[idx]);

                // ---------------GET PowerStart and PowerEnd of the node------------------
                starts[idx] = view.getPowerStart(rp.getNode(n));
                ends[idx] = view.getPowerEnd(rp.getNode(n));
                // ------------------------------------------------------------------------

                durations[idx] = rp.makeUnboundedDuration(rp.makeVarLabel("Dur(", n, ")"));
                solver.post(IntConstraintFactory.arithm(durations[idx], "<=", rp.getEnd()));
                // All tasks have to be scheduled
                heights[idx] = VariableFactory.one(solver);
                taskVars[idx] = VariableFactory.task(starts[idx], durations[idx], ends[idx]);

            }
            solver.post(IntConstraintFactory.cumulative(taskVars, heights, capacity, true));
        }
        // Constraint for discrete model
        List<IntVar> nodesState = new ArrayList<>(constraint.getInvolvedNodes().size());

        for (Node ni : constraint.getInvolvedNodes()) {
            nodesState.add(rp.getNodeAction(ni).getState());
        }

        IntVar mySum = VariableFactory.bounded(rp.makeVarLabel("nbOnline"), 0, constraint.getAmount(), rp.getSolver());
        solver.post(IntConstraintFactory.sum(nodesState.toArray(new IntVar[nodesState.size()]), mySum));
        solver.post(IntConstraintFactory.arithm(mySum, "<=", constraint.getAmount()));

        return true;
    }

    /**
     * The builder associated to this constraint.
     *
     * @author Tu Huynh Dang
     */
    public static class Builder implements ChocoConstraintBuilder {

        @Override
        public ChocoConstraint build(Constraint cstr) {
            return new CMaxOnline((MaxOnline) cstr);
        }

        @Override
        public Class<? extends SatConstraint> getKey() {
            return MaxOnline.class;
        }

    }

}
