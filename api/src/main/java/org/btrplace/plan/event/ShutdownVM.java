/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;


import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.VMState;

import java.util.Objects;

/**
 * An action to stop a virtual machine running on an online node and put it into the ready state.
 *
 * @author Fabien Hermenier
 */
public class ShutdownVM extends Action implements VMStateTransition {

  private final VM vm;

  private final Node node;

    /**
     * Make a new action.
     *
     * @param v     the virtual machine to stop
     * @param on    the hosting node
     * @param start the moment the action starts
     * @param end   the moment the action finish
     */
    public ShutdownVM(VM v, Node on, int start, int end) {
        super(start, end);
        this.vm = v;
        this.node = on;
    }


    /**
     * Apply the action by removing the virtual machine from the model.
     *
     * @param m the model to alter
     * @return {@code true}
     */
    @Override
    public boolean applyAction(Model m) {
        Mapping map = m.getMapping();
        if (map.isOnline(node) &&
                map.isRunning(vm) &&
                map.getVMLocation(vm).equals(node)) {
            map.addReadyVM(vm);
            return true;
        }
        return false;
    }

    @Override
    public String pretty() {
        return "shutdown(" + "vm=" + vm + ", on=" + node + ')';
    }


    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        ShutdownVM that = (ShutdownVM) o;
        return this.vm.equals(that.vm) &&
                this.node.equals(that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), vm, node);
    }

    @Override
    public VM getVM() {
        return vm;
    }

    /**
     * Get the node hosting the VM.
     *
     * @return the node identifier
     */
    public Node getNode() {
        return node;
    }


    @Override
    public VMState getCurrentState() {
        return VMState.RUNNING;
    }

    @Override
    public VMState getNextState() {
        return VMState.READY;
    }

    @Override
    public Object visit(ActionVisitor v) {
        return v.visit(this);
    }
}
