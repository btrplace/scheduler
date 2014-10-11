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

import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;

import java.util.Objects;

/**
 * A event to notify a VM
 * requires a new resource allocation.
 *
 * @author Fabien Hermenier
 */
public class AllocateEvent implements VMEvent {

    private int qty;

    private VM vm;

    private String rc;

    /**
     * Make a new event.
     *
     * @param vmId   the VM that is subject to the resource allocation
     * @param rcId   the resource identifier
     * @param amount the amount of resources to allocate
     */
    public AllocateEvent(VM vmId, String rcId, int amount) {
        this.vm = vmId;
        this.rc = rcId;
        this.qty = amount;
    }

    @Override
    public VM getVM() {
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
        r.setConsumption(vm, qty);
        return true;
    }

    @Override
    public String toString() {
        return "allocate(" + "vm=" + vm + ", rc=" + rc + ", amount=" + qty + ')';
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
        return Objects.hash(qty, rc, vm);
    }

    @Override
    public Object visit(ActionVisitor v) {
        return v.visit(this);
    }
}
