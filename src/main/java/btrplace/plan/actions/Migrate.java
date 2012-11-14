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

package btrplace.plan.actions;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.plan.Action;

import java.util.UUID;

/**
 * Migrate a running VM from one online node to another one.
 *
 * @author Fabien Hermenier
 */
public class Migrate extends Action {

    private UUID vm;

    private UUID src, dst;


    /**
     * Make a new action.
     *
     * @param vm  the VM to migrate
     * @param src the node the VM is currently running on
     * @param dst the node where to place the VM
     * @param st  the moment the action will start
     * @param ed  the moment the action will stop
     */
    public Migrate(UUID vm, UUID src, UUID dst, int st, int ed) {
        super(st, ed);
        this.vm = vm;
        this.src = src;
        this.dst = dst;
    }

    @Override
    public boolean apply(Model i) {
        Mapping c = i.getMapping();
        if (c.getOnlineNodes().contains(src)
                && c.getOnlineNodes().contains(dst)
                && c.getRunningVMs().contains(vm)) {
            c.setVMRunOn(vm, dst);
            return true;
        }
        return false;
    }

    /**
     * Test if this action is equals to another object.
     *
     * @param o the object to compare with
     * @return true if ref is an instance of Instantiate and if both
     *         instance involve the same virtual machine
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            Migrate that = (Migrate) o;
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
     * Get the VM to migrate.
     *
     * @return the VM identifier
     */
    public UUID getVM() {
        return vm;
    }

    /**
     * Get the node that is currently hosting the VM.
     *
     * @return the node identifier
     */
    public UUID getOrigin() {
        return this.src;
    }

    /**
     * Get the node that will receive the VM.
     *
     * @return the node identifier
     */
    public UUID getDestination() {
        return this.dst;
    }

    @Override
    public String toString() {
        return new StringBuilder("migrate(vm=").append(vm)
                .append(", from=").append(src)
                .append(", to=").append(dst)
                .append(')').toString();
    }
}
