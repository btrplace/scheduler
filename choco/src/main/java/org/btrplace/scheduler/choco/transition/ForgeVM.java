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
import org.btrplace.scheduler.SchedulerModelingException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.extensions.TaskMonitor;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;


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

    /**
     * The prefix to use for the variables
     */
    public static final String VAR_PREFIX = "forge";


  private final VM vm;

  private final IntVar duration;

  private final BoolVar state;

  private final String template;

  private final IntVar start;

  private final IntVar end;
    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws SchedulerModelingException if an error occurred
     */
    public ForgeVM(ReconfigurationProblem rp, VM e) throws SchedulerModelingException {
        int d = rp.getDurationEvaluators().evaluate(rp.getSourceModel(), org.btrplace.plan.event.ForgeVM.class, e);
        template = rp.getSourceModel().getAttributes().get(e, "template", "");
        if ("".equals(template)) {
            throw new SchedulerModelingException(rp.getSourceModel(), "Unable to forge the VM '" + e + "'. The required attribute 'template' is missing from the model");
        }
        Model csp = rp.getModel();
        duration = csp.intVar(d);
        state = csp.boolVar(false);
        vm = e;

        /*
         * We don't make any "real" d-slice cause it may impacts the TaskScheduler
         * so the hosting variable is set to -1 to be sure the VM is not hosted on a node
         */

        start = rp.makeUnboundedDuration(VAR_PREFIX, "(", e, ").start");
        end = rp.makeUnboundedDuration(VAR_PREFIX, "(", e, ").stop");
        TaskMonitor.build(start, duration, end);
        csp.post(csp.arithm(duration, ">=", d));
        csp.post(csp.arithm(end, "<=", rp.getEnd()));
    }

    @Override
    public boolean insertActions(Solution s, ReconfigurationPlan plan) {
        org.btrplace.plan.event.ForgeVM a = new org.btrplace.plan.event.ForgeVM(vm, s.getIntVal(getStart()), s.getIntVal(getEnd()));
        return plan.add(a);
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

    @Override
    public VMState getSourceState() {
        return VMState.INIT;
    }

    @Override
    public VMState getFutureState() {
        return VMState.READY;
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
     * The builder devoted to a init -&gt; ready transition.
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
