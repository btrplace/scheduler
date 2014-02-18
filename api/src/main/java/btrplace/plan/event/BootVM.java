/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.plan.event;


import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;

import java.util.Objects;

/**
 * An action that starts a VM on an online node.
 * The VM is originally in the 'ready' state.
 *
 * @author Fabien Hermenier
 */
public class BootVM extends Action implements VMStateTransition, RunningVMPlacement {

    private VM vm;

    private Node node;

    /**
     * Make a new action.
     *
     * @param v     the VM to run
     * @param to    the destination node
     * @param start the moment the action starts.
     * @param end   the moment the action finish
     */
    public BootVM(VM v, Node to, int start, int end) {
        super(start, end);
        this.vm = v;
        this.node = to;
    }

    @Override
    public String pretty() {
        return "boot(" + "vm=" + vm + ", on=" + node + ')';
    }

    @Override
    public Node getDestinationNode() {
        return node;
    }

    @Override
    public VM getVM() {
        return vm;
    }


    @Override
    public boolean applyAction(Model c) {
        return (c.getMapping().isReady(vm) && c.getMapping().addRunningVM(vm, node));
    }

    /**
     * Test if this action is equals to another object.
     *
     * @param o the object to compare with
     * @return true if {@code o} is an instanceof {@link BootVM} and if both
     * instance involve the same VM and the same node
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            BootVM that = (BootVM) o;
            return this.vm.equals(that.vm) &&
                    this.node.equals(that.node) &&
                    this.getStart() == that.getStart() &&
                    this.getEnd() == that.getEnd();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), vm, node);
    }

    @Override
    public VMState getCurrentState() {
        return VMState.READY;
    }

    @Override
    public VMState getNextState() {
        return VMState.RUNNING;
    }

    @Override
    public Object visit(ActionVisitor v) {
        return v.visit(this);
    }
}
