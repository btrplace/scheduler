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

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;

import java.util.Objects;

/**
 * Migrate a running VM from one online node to another one.
 *
 * @author Fabien Hermenier
 */
public class MigrateVM extends Action implements VMEvent, RunningVMPlacement {

    private VM vm;

    private Node src, dst;

    /**
     * Make a new action.
     *
     * @param vm    the VM to migrate
     * @param from  the node the VM is currently running on
     * @param to    the node where to place the VM
     * @param start the moment the action will consume
     * @param end   the moment the action will stop
     */
    public MigrateVM(VM vm, Node from, Node to, int start, int end) {
        super(start, end);
        this.vm = vm;
        this.src = from;
        this.dst = to;
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
    public VM getVM() {
        return vm;
    }

    /**
     * Make the VM running on the destination node
     * in the given model.
     *
     * @param i the model to alter with the action
     * @return {@code true} iff the VM is running on the destination node
     */
    @Override
    public boolean applyAction(Model i) {
        Mapping c = i.getMapping();
        if (c.getOnlineNodes().contains(src)
                && c.getOnlineNodes().contains(dst)
                && c.getRunningVMs().contains(vm)
                && c.getVMLocation(vm).equals(src)
                && !src.equals(dst)) {
            c.addRunningVM(vm, dst);
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            MigrateVM that = (MigrateVM) o;
            return this.vm.equals(that.vm) &&
                    this.src.equals(that.src) &&
                    this.dst.equals(that.dst) &&
                    this.getStart() == that.getStart() &&
                    this.getEnd() == that.getEnd();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), src, dst, vm);
    }

    @Override
    public String pretty() {
        return new StringBuilder("migrate(vm=").append(vm)
                .append(", from=").append(src)
                .append(", to=").append(dst)
                .append(')').toString();
    }

    @Override
    public Object visit(ActionVisitor v) {
        return v.visit(this);
    }
}
