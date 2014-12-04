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
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;


/**
 * Model a transition that will forge a VM to put it into the ready state.
 * <p>
 * The VM must have an attribute (provided by {@link org.btrplace.model.Model#getAttributes()}
 * {@code template} that indicate the template identifier to use to build the VM image.
 * <p>
 * An estimation of the action duration must be provided through a
 * {@link org.btrplace.scheduler.choco.duration.ActionDurationEvaluator} accessible from
 * {@link org.btrplace.scheduler.choco.ReconfigurationProblem#getDurationEvaluators()} with the key {@code ForgeVM.class}
 * <p>
 * If the reconfiguration problem has a solution, a {@link org.btrplace.plan.event.ForgeVM} action
 * will inserted into the resulting reconfiguration plan.
 *
 * @author Fabien Hermenier
 */
public class ForgeVM implements VMTransition {

    private VM vm;

    private IntVar duration;

    private BoolVar state;

    private Slice dSlice;

    private String template;

    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred
     */
    public ForgeVM(ReconfigurationProblem rp, VM e) throws SchedulerException {
        int d = rp.getDurationEvaluators().evaluate(rp.getSourceModel(), org.btrplace.plan.event.ForgeVM.class, e);
        template = rp.getSourceModel().getAttributes().getString(e, "template");
        if (template == null) {
            throw new SchedulerException(rp.getSourceModel(), "Unable to forge the VM '" + e + "'. The required attribute 'template' is missing from the model");
        }
        Solver s = rp.getSolver();
        duration = VariableFactory.fixed(d, s);
        state = VariableFactory.zero(s);
        vm = e;

        /*
         * We don't make any "real" d-slice cause it may impacts the TaskScheduler
         * so the hosting variable is set to -1 to be sure the VM is not hosted on a node
         */
        dSlice = new SliceBuilder(rp, e, rp.makeVarLabel("forge(", e, ").dSlice"))
                .setDuration(duration)
                .setStart(rp.makeUnboundedDuration("forge(", e, ").start"))
                .setEnd(rp.makeUnboundedDuration("forge(", e, ").stop"))
                .setHoster(-1)
                .build();
        s.post(IntConstraintFactory.arithm(dSlice.getDuration(), ">=", d));
        s.post(IntConstraintFactory.arithm(dSlice.getEnd(), "<=", rp.getEnd()));
    }

    @Override
    public boolean insertActions(ReconfigurationPlan plan) {
        org.btrplace.plan.event.ForgeVM a = new org.btrplace.plan.event.ForgeVM(vm, getStart().getValue(), getEnd().getValue());
        return plan.add(a);
    }

    @Override
    public VM getVM() {
        return vm;
    }

    @Override
    public IntVar getStart() {
        return dSlice.getStart();
    }

    @Override
    public IntVar getEnd() {
        return dSlice.getEnd();
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
        return null;
    }

    @Override
    public BoolVar getState() {
        return state;
    }

    @Override
    public boolean isManaged() {
        return true;
    }


    /**
     * Get the template to use to build the VM.
     *
     * @return the template identifier
     */
    public String getTemplate() {
        return template;
    }

    /**
     * The builder devoted to a init->ready transition.
     */
    public static class Builder extends VMTransitionBuilder {

        /**
         * New builder
         */
        public Builder() {
            super("forge", VMState.INIT, VMState.READY);
        }

        @Override
        public VMTransition build(ReconfigurationProblem r, VM v) throws SchedulerException {
            return new ForgeVM(r, v);
        }
    }
}
