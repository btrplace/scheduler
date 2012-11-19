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
 * An action to resume a VirtualMachine on an online node.
 * The state of the virtual machine comes to "sleeping" to "running".
 *
 * @author Fabien Hermenier
 */
public class ResumeVM extends Action {

    private UUID vm;

    private UUID src, dst;

    /**
     * Make a new resume action.
     *
     * @param vm   the virtual machine to resume
     * @param from the source node
     * @param to   the destination node
     * @param st   the moment the action starts.
     * @param end  the moment the action finish
     */
    public ResumeVM(UUID vm, UUID from, UUID to, int st, int end) {
        super(st, end);
        this.vm = vm;
        this.src = from;
        this.dst = to;
    }

    /**
     * Get the VM to resume.
     *
     * @return the VM identifier
     */
    public UUID getVM() {
        return vm;
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
    public String toString() {
        return new StringBuilder("resume(")
                .append("vm=").append(vm)
                .append(", from=").append(src)
                .append(", to=")
                .append(dst).append(')').toString();
    }

    @Override
    public boolean apply(Model m) {
        Mapping map = m.getMapping();
        return (map.getOnlineNodes().contains(src)
                && map.getOnlineNodes().contains(dst)
                && map.getSleepingVMs().contains(vm)
                && map.getVMLocation(vm).equals(src)
                && map.setVMRunOn(vm, dst));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            ResumeVM that = (ResumeVM) o;
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
}
