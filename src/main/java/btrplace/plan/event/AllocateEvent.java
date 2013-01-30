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
import btrplace.model.ShareableResource;
import btrplace.plan.VMEvent;

import java.util.UUID;

/**
 * A event to inform a node that a VM
 * it is hosting may have a new resource allocation.
 *
 * @author Fabien Hermenier
 */
public class AllocateEvent implements VMEvent {

    private int qty;

    private UUID vm;

    private String rc;

    /**
     * Make a new event.
     *
     * @param vm     the VM that is subject to the resource allocation
     * @param rcId   the resource identifier
     * @param amount the amount of resources to allocate
     */
    public AllocateEvent(UUID vm, String rcId, int amount) {
        this.vm = vm;
        this.rc = rcId;
        this.qty = amount;
    }

    @Override
    public UUID getVM() {
        return vm;
    }

    /**
     * Get the resource identifier.
     *
     * @return a non-empty string
     */
    public String getResourceId() {
        return rc;
    }

    /**
     * Get the amount of resources to allocate to the VM.
     *
     * @return a positive number
     */
    public int getAmount() {
        return qty;
    }

    @Override
    public boolean apply(Model i) {
        ShareableResource r = (ShareableResource) i.getView(ShareableResource.VIEW_ID_BASE + rc);
        if (r == null) {
            return false;
        }
        r.set(vm, qty);
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder("allocate(")
                .append("vm=").append(vm)
                .append(", rc=").append(rc)
                .append(", amount=").append(qty)
                .append(')').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            AllocateEvent that = (AllocateEvent) o;
            return this.vm.equals(that.vm)
                    && this.rc.equals(that.rc)
                    && this.qty == that.qty;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int res = qty;
        res = res * 31 + rc.hashCode();
        res = res * 31 + vm.hashCode();
        return res;
    }
}
