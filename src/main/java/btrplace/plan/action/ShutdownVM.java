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

package btrplace.plan.action;


import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.plan.Action;

import java.util.UUID;

/**
 * An action to stop a virtual machine running on an online node and put it into the waiting state.
 *
 * @author Fabien Hermenier
 */
public class ShutdownVM extends Action {

    private UUID vm;

    private UUID node;

    /**
     * Make a new action.
     *
     * @param vm the virtual machine to stop
     * @param on the hosting node
     * @param s  the moment the action start.
     * @param f  the moment the action finish
     */
    public ShutdownVM(UUID vm, UUID on, int s, int f) {
        super(s, f);
        this.vm = vm;
        this.node = on;
    }


    /**
     * Apply the action by removing the virtual machine from the model.
     *
     * @param m the model to alter
     * @return {@code true}
     */
    @Override
    public boolean apply(Model m) {
        Mapping map = m.getMapping();
        if (map.getOnlineNodes().contains(node) &&
                map.getRunningVMs().contains(vm) &&
                map.getVMLocation(vm).equals(node)) {
            map.addWaitingVM(vm);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder("stop(")
                .append("vm=").append(vm)
                .append(", on=").append(node).append(')').toString();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            ShutdownVM that = (ShutdownVM) o;
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

    /**
     * Get the VM to shutdown.
     *
     * @return the VM identifier
     */
    public UUID getVM() {
        return vm;
    }

    /**
     * Get the node hosting the VM.
     *
     * @return the node identifier
     */
    public UUID getNode() {
        return node;
    }
}
