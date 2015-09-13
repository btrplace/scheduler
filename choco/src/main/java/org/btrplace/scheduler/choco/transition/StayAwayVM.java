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
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;


/**
 * A fake action model that indicates the VM
 * is ready or sleeping and does not go in the running state.
 *
 * @author Fabien Hermenier
 */
public class StayAwayVM implements VMTransition {

    private VM vm;

    private BoolVar zero;

    private VMState from, to;
    /**
     * Make a new model.
     *
     * @param from the VM initial state
     * @param to the VM next state
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     */
    public StayAwayVM(VMState from, VMState to, ReconfigurationProblem rp, VM e) {
        vm = e;
        this.from = from;
        this.to = to;
        zero = VariableFactory.zero(rp.getSolver());
    }

    @Override
    public boolean isManaged() {
        return false;
    }

    @Override
    public boolean insertActions(Solution s, ReconfigurationPlan plan) {
        return true;
    }

    @Override
    public VM getVM() {
        return vm;
    }

    @Override
    public IntVar getStart() {
        return zero;
    }

    @Override
    public IntVar getEnd() {
        return zero;
    }

    @Override
    public IntVar getDuration() {
        return zero;
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
        return zero;
    }

    @Override
    public VMState getSourceState() {
        return from;
    }

    @Override
    public VMState getFutureState() {
        return to;
    }

    /**
     * The builder devoted to a ready->ready transition.
     */
    public static class BuilderReady extends VMTransitionBuilder {

        /**
         * New builder
         */
        public BuilderReady() {
            super("stayReady", VMState.READY, VMState.READY);
        }

        @Override
        public VMTransition build(ReconfigurationProblem r, VM v) throws SchedulerException {
            return new StayAwayVM(VMState.READY, VMState.READY, r, v);
        }
    }

    /**
     * The builder devoted to a sleeping->sleeping transition.
     */
    public static class BuilderSleeping extends VMTransitionBuilder {

        /**
         * New builder
         */
        public BuilderSleeping() {
            super("staySleeping", VMState.SLEEPING, VMState.SLEEPING);
        }

        @Override
        public VMTransition build(ReconfigurationProblem r, VM v) throws SchedulerException {
            return new StayAwayVM(VMState.SLEEPING, VMState.SLEEPING, r, v);
        }
    }

    /**
     * The builder devoted to a sleeping->sleeping transition.
     */
    public static class BuilderInit extends VMTransitionBuilder {

        /**
         * New builder
         */
        public BuilderInit() {
            super("stayInit", VMState.INIT, VMState.INIT);
        }

        @Override
        public VMTransition build(ReconfigurationProblem r, VM v) throws SchedulerException {
            return new StayAwayVM(VMState.INIT, VMState.INIT, r, v);
        }
    }
}
