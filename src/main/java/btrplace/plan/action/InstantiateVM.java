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
 * Prepare a VM for being deployed.
 *
 * @author Fabien Hermenier
 */
public class InstantiateVM extends Action {

    private UUID id;

    /**
     * Make a new action.
     *
     * @param vm the VM to instantiate.
     */
    public InstantiateVM(UUID vm, int st, int ed) {
        super(st, ed);
        this.id = vm;
    }

    /**
     * Put the VM in the waiting state if it does not already belong
     * to the mapping.
     *
     * @param m the model to modify
     * @return {@code true} iff successful
     */
    @Override
    public boolean apply(Model m) {
        Mapping map = m.getMapping();

        if (!map.getAllVMs().contains(id)) {
            map.addWaitingVM(id);
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
            InstantiateVM that = (InstantiateVM) o;
            return this.id.equals(that.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int res = getEnd();
        res = getStart() + 31 * res;
        return id.hashCode() + 31 * res;
    }

    /**
     * Get the VM to instantiate.
     *
     * @return the VM identifier
     */
    public UUID getVM() {
        return id;
    }

    @Override
    public String toString() {
        return new StringBuilder("instantiate(vm=").append(id).append(')').toString();
    }
}
