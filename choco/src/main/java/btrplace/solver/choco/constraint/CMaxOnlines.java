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

package btrplace.solver.choco.constraint;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.Constraint;
import btrplace.model.constraint.MaxOnline;
import btrplace.model.constraint.SatConstraint;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.view.CPowerView;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.TaskVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Choco implementation for the {@link MaxOnlines} constraints.
 *
 * @author Tu Huynh Dang
 */
public class CMaxOnlines implements ChocoConstraint {

    private final MaxOnline constraint;

    /**
     * Make a new constraint
     *
     * @param maxOn the constraint to rely on
     */
    public CMaxOnlines(MaxOnline maxOn) {
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
    public boolean inject(ReconfigurationProblem rp) throws SolverException {
        CPSolver solver = rp.getSolver();

        if (constraint.isContinuous()) {
            CPowerView view = (CPowerView) rp.getView(CPowerView.VIEW_ID);
            if (view == null) {
                view = new CPowerView(rp);
                if (!rp.addView(view)) {
                    throw new SolverException(rp.getSourceModel(), "Unable to attach view '" + CPowerView.VIEW_ID + "'");
                }
            }

            final int NUMBER_OF_TASK = constraint.getInvolvedNodes().size();
            int i = 0;
            int[] nodeIdx = new int[NUMBER_OF_TASK];
            for (Node n : constraint.getInvolvedNodes()) {
                nodeIdx[i++] = rp.getNode(n);
            }
            IntDomainVar capacity = solver.createIntegerConstant("capacity", constraint.getAmount());
            IntDomainVar consumption = solver.createBoundIntVar("consum", 0, constraint.getAmount());//minimum consumption
            // of the resource
            IntDomainVar uppBound = rp.getEnd();                    // All tasks must be scheduled before this time
            IntDomainVar[] heights = new IntDomainVar[NUMBER_OF_TASK];   // The state of the node
            IntDomainVar[] starts = new IntDomainVar[NUMBER_OF_TASK];
            IntDomainVar[] ends = new IntDomainVar[NUMBER_OF_TASK];
            IntDomainVar[] Durations = new IntDomainVar[NUMBER_OF_TASK];  // Online duration
            TaskVar[] task_vars = new TaskVar[NUMBER_OF_TASK];  // Online duration is modeled as a task

            for (int idx = 0; idx < nodeIdx.length; idx++) {
                Node n = rp.getNode(nodeIdx[idx]);

                // ---------------GET PowerStart and PowerEnd of the node------------------
                starts[idx] = view.getPowerStart(rp.getNode(n));
                ends[idx] = view.getPowerEnd(rp.getNode(n));
                // ------------------------------------------------------------------------

                Durations[idx] = rp.makeUnboundedDuration("Dur(" + n + ")");
                solver.post(solver.leq(Durations[idx], rp.getEnd()));
                heights[idx] = solver.makeConstantIntVar(1);         // All tasks have to be scheduled
                task_vars[idx] = solver.createTaskVar("Task_" + n, starts[idx], ends[idx], Durations[idx]);
                solver.post(solver.eq(ends[idx], solver.plus(starts[idx], Durations[idx])));
            }
            Cumulative cumulative = new Cumulative(solver, "Cumulative", task_vars,
                    heights, consumption, capacity, uppBound);
            solver.post(cumulative);
        }
//        }
        // Constraint for discrete model
        List<IntDomainVar> nodes_state = new ArrayList<IntDomainVar>(constraint.getInvolvedNodes().size());

        for (Node ni : constraint.getInvolvedNodes()) {
            nodes_state.add(rp.getNodeAction(ni).getState());
        }

        IntExp sum_of_states = CPSolver.sum(nodes_state.toArray(new IntDomainVar[nodes_state.size()]));
        solver.post(solver.leq(sum_of_states, constraint.getAmount()));

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
            return new CMaxOnlines((MaxOnline) cstr);
        }

        @Override
        public Class<? extends SatConstraint> getKey() {
            return MaxOnline.class;
        }

    }

}
