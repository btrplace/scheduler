/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

  private final int qty;

  private final VM vm;

  private final String rc;

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
        ShareableResource r = ShareableResource.get(i, rc);
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
