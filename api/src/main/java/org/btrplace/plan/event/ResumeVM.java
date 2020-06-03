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
 * An action to resume a VirtualMachine on an online node.
 * The state of the virtual machine comes to "sleeping" to "running".
 *
 * @author Fabien Hermenier
 */
public class ResumeVM extends Action implements VMStateTransition, RunningVMPlacement {

  private final VM vm;

  private final Node src;
  private final Node dst;

  /**
   * Make a new resume action.
   *
   * @param v     the virtual machine to resume
   * @param from  the source node
   * @param to    the destination node
   * @param start the moment the action starts.
   * @param end   the moment the action finish
   */
    public ResumeVM(VM v, Node from, Node to, int start, int end) {
        super(start, end);
        this.vm = v;
        this.src = from;
        this.dst = to;
    }

    @Override
    public VM getVM() {
        return vm;
    }

    @Override
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
    public String pretty() {
        return "resume(" + "vm=" + vm + ", from=" + src + ", to=" + dst + ')';
    }

    @Override
    public boolean applyAction(Model m) {
        Mapping map = m.getMapping();
        return map.isSleeping(vm)
                && map.getVMLocation(vm).equals(src)
                && map.addRunningVM(vm, dst);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        ResumeVM that = (ResumeVM) o;
        return this.vm.equals(that.vm) &&
                this.src.equals(that.src) &&
                this.dst.equals(that.dst);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), src, dst, vm);
    }


    @Override
    public VMState getCurrentState() {
        return VMState.SLEEPING;
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
