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

import btrplace.model.constraint.checker.PreserveChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Ensure the allocation of a given minimum amount of resources for
 * each of the given VMs. If a VM is not running, the constraint ignores it.
 * The amount to allocate must be specified as a minimum or an exact value.
 * At most, the VM will have an allocation of resources equals to the maximum allowed
 * <p/>
 * The restriction provided by the constraint is discrete.
 *
 * @author Fabien Hermenier
 */
public class Preserve extends SatConstraint {

    private int amount;

    private String rc;

    /**
     * Make a new constraint.
     *
     * @param vms    the VMs identifiers
     * @param rcId   the resource identifier
     * @param amount the minimum amount of resource to allocate
     */
    public Preserve(Set<UUID> vms, String rcId, int amount) {
        super(vms, Collections.<UUID>emptySet(), false);
        this.rc = rcId;
        this.amount = amount;
    }

    /**
     * Get the resource identifier.
     *
     * @return the identifier
     */
    public String getResource() {
        return this.rc;
    }

    /**
     * Get the amount of resources.
     *
     * @return a positive integer
     */
    public int getAmount() {
        return this.amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Preserve that = (Preserve) o;
        return getInvolvedVMs().equals(that.getInvolvedVMs()) &&
                amount == that.amount &&
                rc.equals(that.rc);
    }

    @Override
    public int hashCode() {
        return getInvolvedVMs().hashCode() + 31 * amount + 31 * rc.hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder("preserve(vms=")
                .append(getInvolvedVMs())
                .append(", rc=").append(rc)
                .append(", amount=").append(amount)
                .append(", discrete")
                .append(')').toString();
    }

    @Override
    public boolean setContinuous(boolean b) {
        if (!b) {
            return super.setContinuous(b);
        }
        return !b;
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new PreserveChecker(this);
    }

}


