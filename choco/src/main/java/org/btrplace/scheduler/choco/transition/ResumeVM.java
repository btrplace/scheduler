/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.VMState;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.SliceBuilder;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;


/**
 * Model an action that resume a sleeping VM.
 * An estimation of the action duration must be provided through a
 * {@link org.btrplace.scheduler.choco.duration.ActionDurationEvaluator} accessible from
 * {@link org.btrplace.scheduler.choco.ReconfigurationProblem#getDurationEvaluators()} with the key {@code ResumeVM.class}
 * <p>
 * If the reconfiguration problem has a solution, a {@link org.btrplace.plan.event.ResumeVM} action
 * is inserted into the resulting reconfiguration plan.
 *
 * @author Fabien Hermenier
 */
public class ResumeVM implements VMTransition {

  public static final String PREFIX = "resumeVM(";
  private final VM vm;
  private final ReconfigurationProblem rp;
  private final IntVar start;
  private final IntVar end;
  private final IntVar duration;
  private final Slice dSlice;
  private final BoolVar state;

  /**
   * Make a new model.
   *
   * @param p the RP to use as a basis.
   * @param e the VM managed by the action
   * @throws org.btrplace.scheduler.SchedulerException if an error occurred
   */
  public ResumeVM(ReconfigurationProblem p, VM e) throws SchedulerException {
    this.rp = p;
        this.vm = e;

        int d = p.getDurationEvaluators().evaluate(p.getSourceModel(), org.btrplace.plan.event.ResumeVM.class, e);

        Model csp = p.getModel();

        start = p.makeDuration(p.getEnd().getUB() - d, 0, PREFIX, e, ").start");
        end = rp.getModel().intOffsetView(start, d);
        duration = p.makeDuration(d, d, PREFIX, e, ").duration");
        dSlice = new SliceBuilder(p, e, PREFIX, e, ").dSlice").setStart(start)
                .setDuration(p.makeDuration(p.getEnd().getUB(), d, PREFIX, e, ").dSlice_duration"))
                .build();

        csp.post(csp.arithm(end, "<=", p.getEnd()));
        state = csp.boolVar(true);
    }

    @Override
    public boolean isManaged() {
        return true;
    }

    @Override
    public boolean insertActions(Solution s, ReconfigurationPlan plan) {
        int ed = s.getIntVal(end);
        int st = s.getIntVal(start);
        Node src = rp.getSourceModel().getMapping().getVMLocation(vm);
        Node dst = rp.getNode(s.getIntVal(dSlice.getHoster()));
        org.btrplace.plan.event.ResumeVM a = new org.btrplace.plan.event.ResumeVM(vm, src, dst, st, ed);
        plan.add(a);
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
    public VMState getSourceState() {
        return VMState.SLEEPING;
    }

    @Override
    public VMState getFutureState() {
        return VMState.RUNNING;
    }

    /**
     * The builder devoted to a sleeping -&gt; running transition.
     */
    public static class Builder extends VMTransitionBuilder {

        /**
         * New builder
         */
        public Builder() {
            super("resume", VMState.SLEEPING, VMState.RUNNING);
        }

        @Override
        public VMTransition build(ReconfigurationProblem r, VM v) throws SchedulerException {
            return new ResumeVM(r, v);
        }
    }
}
