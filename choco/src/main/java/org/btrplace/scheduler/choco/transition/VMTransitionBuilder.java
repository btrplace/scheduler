/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.VM;
import org.btrplace.model.VMState;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;

import java.util.EnumSet;
import java.util.Set;

/**
 * A builder to instantiate a {@link VMTransitionBuilder}
 *
 * @author Fabien Hermenier
 */
public abstract class VMTransitionBuilder {

  /**
   * The possible initial states of the VM.
   */
  private final Set<VMState> s;

    /**
     * The next state of the VM.
     */
    private final VMState d;

  private final String id;

  /**
     * New builder.
     *
     * @param lbl the action identifier
     * @param src the possible initial states of the VM.
     * @param dst the destination state of the VM
     */
    protected VMTransitionBuilder(String lbl, Set<VMState> src, VMState dst) {
        this.s = src;
        this.d = dst;
        this.id = lbl;
    }

    /**
     * New builder.
     *
     * @param lbl the action identifier
     * @param src the initial state of the VM.
     * @param dst the destination state of the VM
     */
    protected VMTransitionBuilder(String lbl, VMState src, VMState dst) {
        this(lbl, EnumSet.of(src), dst);
    }

    /**
     * Build the {@link VMTransition}
     *
     * @param rp the current problem
     * @param v  the manipulated VM
     * @return the resulting model
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred while building the model
     */
    public abstract VMTransition build(ReconfigurationProblem rp, VM v) throws SchedulerException;

    /**
     * Get the initial state of the VM.
     *
     * @return a state
     */
    public Set<VMState> getSourceStates() {
        return s;
    }

    /**
     * Get the destination state of the VM
     *
     * @return a state.
     */
    public VMState getDestinationState() {
        return d;
    }

    @Override
    public String toString() {
        return s + " -> " + d + ": " + id;
    }

}
