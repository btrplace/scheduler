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

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.VM;
import org.btrplace.model.VMState;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.SliceBuilder;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * Model an action that stop a running VM.
 * An estimation of the action duration must be provided through a
 * {@link org.btrplace.scheduler.choco.duration.ActionDurationEvaluator} accessible from
 * {@link org.btrplace.scheduler.choco.ReconfigurationProblem#getDurationEvaluators()} with the key {@code ShutdownVM.class}
 * <p>
 * If the reconfiguration problem has a solution, a {@link org.btrplace.plan.event.ShutdownVM} action is inserted
 * into the resulting reconfiguration plan.
 *
 * @author Fabien Hermenier
 */
public class ShutdownVM implements VMTransition {

    private ReconfigurationProblem rp;

    private VM vm;

    private IntVar duration;

    private Slice cSlice;

    private IntVar start;

    private BoolVar state;

    /**
     * Make a new model.
     *
     * @param p the RP to use as a basis.
     * @param e the VM managed by the action
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred
     */
    public ShutdownVM(ReconfigurationProblem p, VM e) throws SchedulerException {
        this.rp = p;
        this.vm = e;

        int d = p.getDurationEvaluators().evaluate(p.getSourceModel(), org.btrplace.plan.event.ShutdownVM.class, e);
        assert d > 0;
        duration = p.makeDuration(d, d, "shutdownVM(", e, ").duration");
        this.cSlice = new SliceBuilder(p, e, "shutdownVM(" + e + ").cSlice").setHoster(p.getCurrentVMLocation(p.getVM(e)))
                .setEnd(p.makeDuration(p.getEnd().getUB(), d, "shutdownVM(", e, ").cSlice_end"))
                .build();
        start = VariableFactory.offset(cSlice.getEnd(), -d);
        state = VariableFactory.zero(rp.getSolver());
        rp.getSolver().post(IntConstraintFactory.arithm(cSlice.getEnd(), "<=", p.getEnd()));
    }

    @Override
    public boolean isManaged() {
        return true;
    }


    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        plan.add(new org.btrplace.plan.event.ShutdownVM(getVM(),
                rp.getSourceModel().getMapping().getVMLocation(getVM()),
                start.getValue(),
                cSlice.getEnd().getValue()));
        return true;
    }

    @Override
    public VM getVM() {
        return vm;
    }

    @Override
    public IntVar getStart() {
        return start;
    }

    @Override
    public IntVar getEnd() {
        return cSlice.getEnd();
    }

    @Override
    public IntVar getDuration() {
        return duration;
    }

    @Override
    public Slice getCSlice() {
        return cSlice;
    }

    @Override
    public Slice getDSlice() {
        return null;
    }

    @Override
    public BoolVar getState() {
        return state;
    }

    /**
     * The builder devoted to a running->ready transition.
     */
    public static class Builder extends VMTransitionBuilder {

        /**
         * New builder
         */
        public Builder() {
            super("shutdown", VMState.RUNNING, VMState.READY);
        }

        @Override
        public VMTransition build(ReconfigurationProblem r, VM v) throws SchedulerException {
            return new ShutdownVM(r, v);
        }
    }
}
