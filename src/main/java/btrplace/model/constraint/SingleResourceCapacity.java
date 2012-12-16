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

package btrplace.model.constraint;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.ShareableResource;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Restrict the capacity of each of the given server to a given
 * amount for a specific resource that is used by the VMs.
 *
 * @author Fabien Hermenier
 */
public class SingleResourceCapacity extends SatConstraint {

    private String rcId;

    private int amount;

    /**
     * Make a new constraint.
     *
     * @param nodes  the involved servers.
     * @param rcId   the resource identifier
     * @param amount the maximum amount of resource to share among the hosted VMs
     */
    public SingleResourceCapacity(Set<UUID> nodes, String rcId, int amount) {
        super(Collections.<UUID>emptySet(), nodes, false);
        this.rcId = rcId;
        this.amount = amount;
    }

    /**
     * Get the amount of resources
     *
     * @return a positive integer
     */
    public int getAmount() {
        return this.amount;
    }

    /**
     * Get the resource identifier.
     *
     * @return a String
     */
    public String getResource() {
        return this.rcId;
    }

    @Override
    public Sat isSatisfied(Model i) {
        ShareableResource rc = i.getResource(rcId);
        if (rc == null) {
            return Sat.UNSATISFIED;
        }
        return Sat.UNDEFINED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SingleResourceCapacity that = (SingleResourceCapacity) o;
        return getInvolvedNodes().equals(that.getInvolvedNodes())
                && amount == that.amount && rcId.equals(that.rcId);
    }

    @Override
    public int hashCode() {
        int res = amount;
        res = 31 * res + rcId.hashCode();
        res = 31 * res + getInvolvedNodes().hashCode();
        return res;
    }

    @Override
    public String toString() {
        return new StringBuilder("singleResourceCapacity(")
                .append("nodes=").append(getInvolvedNodes())
                .append(", rc=").append(rcId)
                .append(", amount=").append(amount)
                .append(")").toString();
    }
}
