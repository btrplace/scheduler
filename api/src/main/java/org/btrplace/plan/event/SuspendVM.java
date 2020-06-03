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
 * An action that suspend a running virtual machine to disk.
 *
 * @author Fabien Hermenier
 */
public class SuspendVM extends Action implements VMStateTransition {

  private final VM vm;

  private final Node src;
  private final Node dst;

  /**
   * Make a new suspend action.
   *
   * @param v     the virtual machine to suspend
   * @param from  The node that host the virtual machine
   * @param to    the destination node.
   * @param start the moment the action starts.
   * @param end   the moment the action finish
   */
    public SuspendVM(VM v, Node from, Node to, int start, int end) {
        super(start, end);
        this.vm = v;
        this.src = from;
        this.dst = to;

    }

    @Override
    public String pretty() {
        return "suspend(" + "vm=" + vm + ", from=" + src + ", to=" + dst + ')';
    }

    /**
     * Apply the action by putting the VM
     * into the sleeping state on its destination node in a given model
     *
     * @param m the model to alter
     * @return {@code true} iff the VM is now sleeping on the destination node
     */
    @Override
    public boolean applyAction(Model m) {
        Mapping map = m.getMapping();
        return map.isRunning(vm) &&
                map.getVMLocation(vm).equals(src) &&
                map.addSleepingVM(vm, dst);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        SuspendVM that = (SuspendVM) o;
        return this.vm.equals(that.vm) &&
                this.src.equals(that.src) &&
                this.dst.equals(that.dst);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), src, dst, vm);
    }

    /**
     * Get the destination node.
     *
     * @return the node identifier
     */
    public Node getDestinationNode() {
        return dst;
    }

    /**
     * Get the source node that is currently hosting the VM.
     *
     * @return the node identifier
     */
    public Node getSourceNode() {
        return src;
    }

    @Override
    public VM getVM() {
        return vm;
    }


    @Override
    public VMState getCurrentState() {
        return VMState.RUNNING;
    }

    @Override
    public VMState getNextState() {
        return VMState.SLEEPING;
    }

    @Override
    public Object visit(ActionVisitor v) {
        return v.visit(this);
    }
}
