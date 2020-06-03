/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
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

import java.util.EnumSet;


/**
 * An action to model a VM that is killed.
 * An estimation of the action duration must be provided through a
 * {@link org.btrplace.scheduler.choco.duration.ActionDurationEvaluator} accessible from
 * {@link org.btrplace.scheduler.choco.ReconfigurationProblem#getDurationEvaluators()} with the key {@code KillVM.class}
 * <p>
 * If the reconfiguration problem has a solution, a {@link org.btrplace.plan.event.KillVM} action
 * is inserted into the resulting reconfiguration plan.
 * <p>
 * The kill necessarily occurs at the beginning of the reconfiguration process and
 * can consider a VM that is either in the ready, the running, and the sleeping state.
 *
 * @author Fabien Hermenier
 */
public class KillVM implements VMTransition {

  private final VM vm;

  private final Node node;

  private final BoolVar state;

  private final IntVar start;

  private final IntVar end;

    private Slice cSlice;

  private final VMState from;
    /**
     * Make a new model.
     *
     * @param from the initial VM state.
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws SchedulerException if an error occurred
     */
    public KillVM(VMState from, ReconfigurationProblem rp, VM e) throws SchedulerException {
        vm = e;
        this.from = from;
        Mapping map = rp.getSourceModel().getMapping();
        node = map.getVMLocation(vm);
        state = rp.getModel().boolVar(false);

        int d = rp.getDurationEvaluators().evaluate(rp.getSourceModel(), org.btrplace.plan.event.KillVM.class, e);

        if (map.isRunning(vm)) {
            cSlice = new SliceBuilder(rp, e, "killVM('" + e + "').cSlice")
                    .setStart(rp.getStart())
                    .setHoster(rp.getCurrentVMLocation(rp.getVM(vm)))
                    .setEnd(rp.getModel().intVar(d))
                    .build();
            end = cSlice.getEnd();
        } else {
            end = rp.getModel().intVar(d);
        }
        start = rp.getStart();
    }

    @Override
    public boolean isManaged() {
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
        return end;
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
    public VM getVM() {
        return vm;
    }

    @Override
    public boolean insertActions(Solution s, ReconfigurationPlan plan) {
        plan.add(new org.btrplace.plan.event.KillVM(vm, node, s.getIntVal(getStart()), s.getIntVal(getEnd())));
        return true;
    }

    @Override
    public BoolVar getState() {
        return state;
    }

    @Override
    public VMState getSourceState() {
        return from;
    }

    @Override
    public VMState getFutureState() {
        return VMState.KILLED;
    }
    /**
     * The builder devoted to a (init|ready|running|sleep) &gt; killed transition.
     */
    public static class Builder extends VMTransitionBuilder {

        /**
         * New builder
         */
        public Builder() {
            super("kill", EnumSet.of(VMState.INIT, VMState.READY, VMState.RUNNING, VMState.SLEEPING), VMState.KILLED);
        }

        @Override
        public VMTransition build(ReconfigurationProblem r, VM v) throws SchedulerException {
            Mapping m = r.getSourceModel().getMapping();
            if (m.isReady(v)) {
                return new KillVM(VMState.READY, r, v);
            } else if (m.isRunning(v)) {
                return new KillVM(VMState.RUNNING, r, v);
            }  else if (m.isSleeping(v)) {
                return new KillVM(VMState.SLEEPING, r, v);
            }
            return new KillVM(VMState.INIT, r, v);
        }
    }
}
