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
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;

import java.util.EnumSet;

/**
 * A builder to instantiate a {@link VMTransitionBuilder}
 *
 * @author Fabien Hermenier
 */
public abstract class VMTransitionBuilder {

    /**
     * The possible initial states of the VM.
     */
    private EnumSet<VMState> s;

    /**
     * The next state of the VM.
     */
    private VMState d;

    private String id;

    /**
     * New builder.
     *
     * @param lbl the action identifier
     * @param src the possible initial states of the VM.
     * @param dst the destination state of the VM
     */
    public VMTransitionBuilder(String lbl, EnumSet<VMState> src, VMState dst) {
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
    public VMTransitionBuilder(String lbl, VMState src, VMState dst) {
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
    public EnumSet<VMState> getSourceStates() {
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
