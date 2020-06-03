/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.VM;
import org.btrplace.model.VMState;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;


/**
 * A fake action model that indicates the VM
 * is ready or sleeping and does not go in the running state.
 *
 * @author Fabien Hermenier
 */
public class StayAwayVM implements VMTransition {

  private final VM vm;

  private final BoolVar zero;

  private final VMState from;
  private final VMState to;

  /**
   * Make a new model.
   *
   * @param from the VM initial state
   * @param to   the VM next state
   * @param rp   the RP to use as a basis.
   * @param e    the VM managed by the action
   */
  public StayAwayVM(VMState from, VMState to, ReconfigurationProblem rp, VM e) {
    vm = e;
        this.from = from;
        this.to = to;
        zero = rp.getModel().boolVar(false);
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

  @Override
  public String toString() {
    return "stayAway(" +
            "vm=" + vm +
            ", from=" + from +
            ", to=" + to +
            ')';
  }

    /**
     * The builder devoted to a ready -&gt; ready transition.
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
     * The builder devoted to a sleeping -&gt; sleeping transition.
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
     * The builder devoted to a sleeping -&gt; sleeping transition.
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
