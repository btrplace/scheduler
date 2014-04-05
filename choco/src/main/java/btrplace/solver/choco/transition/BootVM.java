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

package btrplace.solver.choco.transition;

import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.VMState;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.Slice;
import btrplace.solver.choco.SliceBuilder;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.EnumSet;


/**
 * Model a transition that allows a ready VP to be booted on a node.
 * An estimation of the action duration must be provided through a
 * {@link btrplace.solver.choco.duration.ActionDurationEvaluator} accessible from
 * {@link btrplace.solver.choco.ReconfigurationProblem#getDurationEvaluators()} with the key {@code BootVM.class}
 * <p/>
 * If the reconfiguration problem has a solution, a {@link btrplace.plan.event.BootVM} action
 * is inserted into the resulting reconfiguration plan.
 *
 * @author Fabien Hermenier
 */
public class BootVM implements VMTransition {

    private Slice dSlice;

    private IntVar end;

    private IntVar start;

    private IntVar duration;

    private VM vm;

    private ReconfigurationProblem rp;

    private BoolVar state;

    /**
     * Make a new model.
     *
     * @param p the RP to use as a basis.
     * @param e the VM managed by the action
     * @throws SolverException if an error occurred
     */
    public BootVM(ReconfigurationProblem p, VM e) throws SolverException {
        vm = e;

        int d = p.getDurationEvaluators().evaluate(p.getSourceModel(), btrplace.plan.event.BootVM.class, e);
        this.rp = p;
        start = p.makeDuration(p.getEnd().getUB() - d, 0, "bootVM(", e, ").start");
        end = VariableFactory.offset(start, d);
        duration = p.makeDuration(d, d, "bootVM.duration(", e, ')');
        dSlice = new SliceBuilder(p, e, "bootVM(" + e + ").dSlice").setStart(start)
                .setDuration(p.makeDuration(p.getEnd().getUB(), d, "bootVM(", e, ").dSlice_duration"))
                .build();
        Solver s = p.getSolver();
        s.post(IntConstraintFactory.arithm(start, "<=", p.getEnd()));
        s.post(IntConstraintFactory.arithm(end, "<=", p.getEnd()));
        s.post(IntConstraintFactory.arithm(duration, "<=", p.getEnd()));

        state = VariableFactory.one(rp.getSolver());
    }

    @Override
    public boolean isManaged() {
        return true;
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        Node node = rp.getNode(dSlice.getHoster().getValue());
        btrplace.plan.event.BootVM a = new btrplace.plan.event.BootVM(vm, node, start.getValue(), end.getValue());
        plan.add(a);
        return true;
    }

    @Override
    public IntVar getStart() {
        return start;
    }

    @Override
    public IntVar getEnd() {
        return end;
    }

    @Override
    public IntVar getDuration() {
        return duration;
    }

    @Override
    public Slice getCSlice() {
        return null;
    }

    @Override
    public Slice getDSlice() {
        return dSlice;
    }

    @Override
    public BoolVar getState() {
        return state;
    }

    @Override
    public VM getVM() {
        return vm;
    }

    /**
     * The builder devoted to a ready->running transition.
     */
    public static class Builder extends VMTransitionBuilder {

        /**
         * New builder
         */
        public Builder() {
            super("boot", EnumSet.of(VMState.READY), VMState.RUNNING);
        }

        @Override
        public VMTransition build(ReconfigurationProblem r, VM v) throws SolverException {
            return new BootVM(r, v);
        }
    }
}
