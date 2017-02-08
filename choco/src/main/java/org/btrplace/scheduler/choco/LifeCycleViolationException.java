/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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
