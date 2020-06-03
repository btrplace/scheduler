/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.NodeState;
import org.btrplace.model.VM;
import org.btrplace.model.VMState;
import org.btrplace.scheduler.SchedulerModelingException;

import java.util.Set;

/**
 * Signals there is no model for a required transition.
 * @author Fabien Hermenier
 */
public class LifeCycleViolationException extends SchedulerModelingException {

    /**
     * An exception related to a VM state transition.
     *
     * @param mo  the source model
     * @param v   the involved VM
     * @param cur the current state
     * @param dst the expected destination state
     */
    public LifeCycleViolationException(Model mo, VM v, VMState cur, VMState dst) {
        super(mo, "No model available for VM '" + v + "' state transition " + cur + " -> " + dst);
    }

    /**
     * An exception related to a Node state transition.
     * @param mo the source model
     * @param n the involved node
     * @param cur the current state
     * @param dst the expected destination state
     */
    public LifeCycleViolationException(Model mo, Node n, NodeState cur, Set<NodeState> dst) {
        super(mo, "No model available for node '" + n + "' state transition " + cur + " -> " + dst);
    }

}
