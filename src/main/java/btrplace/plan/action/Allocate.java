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

import btrplace.model.Model;
import btrplace.model.ShareableResource;
import btrplace.plan.Action;

import java.util.UUID;

/**
 * An action to indicate the amount of resource of a given type
 * to allocate to a VM.
 *
 * @author Fabien Hermenier
 */
public class Allocate extends Action {

    private UUID vm;

    private UUID node;

    private String rcId;

    private int amount;

    /**
     * Make a new constraint.
     *
     * @param vm     the VM identifier
     * @param host   the identifier of the node hosting the VM
     * @param rcId   the resource identifier
     * @param amount the minimum amount of resource to allocate
     * @param st     the moment the action starts
     * @param ed     the moment the action ends
     */
    public Allocate(UUID vm, UUID host, String rcId, int amount, int st, int ed) {
        super(st, ed);
        this.vm = vm;
        this.node = host;
        this.rcId = rcId;
        this.amount = amount;
    }

    /**
     * Get the node that is currently hosting the VM.
     *
     * @return the node identifier
     */
    public UUID getHost() {
        return node;
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
     * Get the resource identifier.
     *
     * @return a non-empty string
     */
    public String getResourceId() {
        return rcId;
    }

    /**
     * Get the amount of resources to allocate to the VM.
     *
     * @return a positive number
     */
    public int getAmount() {
        return amount;
    }

    @Override
    public boolean apply(Model i) {
        ShareableResource rc = i.getResource(rcId);
        if (rc == null) {
            return false;
        }
        rc.set(vm, amount);
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder("allocate(")
                .append("vm=").append(vm)
                .append(", on=").append(node)
                .append(", rc=").append(rcId)
                .append(", amount=").append(amount)
                .append(')').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() == this.getClass()) {
            Allocate that = (Allocate) o;
            return this.vm.equals(that.vm)
                    && this.node.equals(that.node)
                    && this.rcId.equals(that.rcId)
                    && this.getStart() == that.getStart()
                    && this.getEnd() == that.getEnd()
                    && this.amount == that.amount;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int res = getEnd();
        res = getStart() + 31 * res;
        res = res * 31 + amount;
        res = res * 31 + rcId.hashCode();
        res = res * 31 + vm.hashCode();
        res = res + 31 + node.hashCode();
        return res;
    }
}
