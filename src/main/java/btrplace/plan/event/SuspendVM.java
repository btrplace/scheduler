/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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
import btrplace.plan.Action;
import btrplace.plan.VMEvent;

import java.util.UUID;

/**
 * An action that suspend a running virtual machine to disk.
 *
 * @author Fabien Hermenier
 */
public class SuspendVM extends Action implements VMEvent {

    private UUID vm;

    private UUID src, dst;

    /**
     * Make a new suspend action.
     *
     * @param vm   the virtual machine to suspend
     * @param from The node that host the virtual machine
     * @param to   the destination node.
     * @param s    the moment the action starts.
     * @param f    the moment the action finish
     */
    public SuspendVM(UUID vm, UUID from, UUID to, int s, int f) {
        super(s, f);
        this.vm = vm;
        this.src = from;
        this.dst = to;

    }

    @Override
    public String pretty() {
        return new StringBuilder("suspend(")
                .append("vm=").append(vm)
                .append(", from=").append(src)
                .append(", to=").append(dst).append(')').toString();
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
        return (map.getOnlineNodes().contains(src) &&
                map.getOnlineNodes().contains(dst) &&
                map.getRunningVMs().contains(vm) &&
                map.getVMLocation(vm).equals(src) &&
                map.addSleepingVM(vm, dst)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            SuspendVM that = (SuspendVM) o;
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
        int res = getEnd();
        res = getStart() + 31 * res;
        res = src.hashCode() + 31 * res;
        res = 31 * res + dst.hashCode();
        return 31 * res + src.hashCode();
    }

    /**
     * Get the destination node.
     *
     * @return the node identifier
     */
    public UUID getDestinationNode() {
        return dst;
    }

    /**
     * Get the source node that is currently hosting the VM.
     *
     * @return the node identifier
     */
    public UUID getSourceNode() {
        return src;
    }

    @Override
    public UUID getVM() {
        return vm;
    }
}
