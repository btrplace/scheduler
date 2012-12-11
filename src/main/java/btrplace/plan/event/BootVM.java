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


import btrplace.model.Model;
import btrplace.plan.Action;
import btrplace.plan.VMEvent;

import java.util.UUID;

/**
 * An action that demand to run a virtual machine on an online node.
 * The virtual machine is originally in the state 'ready'.
 *
 * @author Fabien Hermenier
 */
public class BootVM extends Action implements VMEvent {

    private UUID vm;

    private UUID node;

    /**
     * Make a new time-bounded run.
     *
     * @param vm  the virtual machine to run
     * @param to  the destination node
     * @param st  the moment the action starts.
     * @param end the moment the action finish
     */
    public BootVM(UUID vm, UUID to, int st, int end) {
        super(st, end);
        this.vm = vm;
        this.node = to;
    }

    @Override
    public String pretty() {
        return new StringBuilder("boot(")
                .append("vm=").append(vm)
                .append(", on=").append(node)
                .append(')').toString();
    }

    /**
     * Get the destination node.
     *
     * @return the node identifier
     */
    public UUID getDestinationNode() {
        return node;
    }

    @Override
    public UUID getVM() {
        return vm;
    }


    @Override
    public boolean applyAction(Model c) {
        return (c.getMapping().getReadyVMs().contains(vm) && c.getMapping().addRunningVM(vm, node));
    }

    /**
     * Test if this action is equals to another object.
     *
     * @param o the object to compare with
     * @return true if ref is an instanceof Run and if both
     *         instance involve the same virtual machine and the same nodes
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
        int res = getEnd();
        res = getStart() + 31 * res;
        res = vm.hashCode() + 31 * res;
        return 31 * res + node.hashCode();
    }
}
