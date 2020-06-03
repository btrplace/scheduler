/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;


import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.VMState;

import java.util.Objects;

/**
 * An action that starts a VM on an online node.
 * The VM is originally in the 'ready' state.
 *
 * @author Fabien Hermenier
 */
public class BootVM extends Action implements VMStateTransition, RunningVMPlacement {

  private final VM vm;

  private final Node node;

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
        return c.getMapping().isReady(vm) && c.getMapping().addRunningVM(vm, node);
    }

    /**
     * Test if this action is equals to another object.
     *
     * @param o the object to compare with
     * @return true if {@code o} is an instanceof BootVM and if both
     * instances involve the same VM and the same node
     */
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        BootVM that = (BootVM) o;
        return this.vm.equals(that.vm) &&
                this.node.equals(that.node);
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
