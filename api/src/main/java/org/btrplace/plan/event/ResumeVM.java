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

package org.btrplace.plan.event;


import org.btrplace.model.*;

import java.util.Objects;

/**
 * An action to resume a VirtualMachine on an online node.
 * The state of the virtual machine comes to "sleeping" to "running".
 *
 * @author Fabien Hermenier
 */
public class ResumeVM extends Action implements VMStateTransition, RunningVMPlacement {

    private VM vm;

    private Node src, dst;

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
        return (map.isOnline(src)
                && map.isOnline(dst)
                && map.isSleeping(vm)
                && map.getVMLocation(vm) == src
                && map.addRunningVM(vm, dst));
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
