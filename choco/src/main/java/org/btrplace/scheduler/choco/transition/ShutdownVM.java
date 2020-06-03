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
import org.btrplace.scheduler.choco.SliceBuilder;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;


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

    /**
     * The prefix to use for the variables
     */
    public static final String VAR_PREFIX = "shutdownVM";

  private final ReconfigurationProblem rp;

  private final VM vm;

  private final IntVar duration;

  private final Slice cSlice;

  private final IntVar start;

  private final BoolVar state;

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
        duration = p.makeDuration(d, d, VAR_PREFIX, "(", e, ").duration");
        this.cSlice = new SliceBuilder(p, e, VAR_PREFIX, "(" + e + ").cSlice").setHoster(p.getCurrentVMLocation(p.getVM(e)))
                .setEnd(p.makeDuration(p.getEnd().getUB(), d, VAR_PREFIX, "(", e, ").cSlice_end"))
                .build();
        start = rp.getModel().intOffsetView(cSlice.getEnd(), -d);
        state = rp.getModel().boolVar(false);
    }

    @Override
    public boolean isManaged() {
        return true;
    }


    @Override
    public boolean insertActions(Solution s, ReconfigurationPlan plan) {
        plan.add(new org.btrplace.plan.event.ShutdownVM(getVM(),
                rp.getSourceModel().getMapping().getVMLocation(getVM()),
                s.getIntVal(start),
                s.getIntVal(cSlice.getEnd())));
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

    @Override
    public VMState getSourceState() {
        return VMState.RUNNING;
    }

    @Override
    public VMState getFutureState() {
        return VMState.READY;
    }

    @Override
    public String toString() {
        return "shutdownVM(" +
                "vm=" + vm +
                ')';
    }

    /**
     * The builder devoted to a running -&gt; ready transition.
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
