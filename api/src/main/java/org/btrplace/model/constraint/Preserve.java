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

package org.btrplace.model.constraint;

import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.*;

/**
 * Ensure the allocation of a given minimum amount of resources for
 * the given VM. If it is not running, the constraint ignores it.
 * The amount to allocate must be specified as a minimum or an exact value.
 * At most, the VM will have an allocation of resources equals to the maximum allowed
 * <p>
 * The restriction provided by the constraint is discrete.
 *
 * @author Fabien Hermenier
 */
public class Preserve extends SatConstraint {

    private int amount;

    private String rc;

    /**
     * Make multiple constraints
     *
     * @param vms the VMs involved in the constraints
     * @param r   the resource identifier
     * @param q   the the minimum amount of resources to allocate to each VM. >= 0
     * @return a list of constraints
     */
    public static List<Preserve> newPreserve(Collection<VM> vms, String r, int q) {
        List<Preserve> l = new ArrayList<>(vms.size());
        for (VM v : vms) {
            l.add(new Preserve(v, r, q));
        }
        return l;
    }

    /**
     * Make a new constraint.
     *
     * @param vm the VM
     * @param r  the resource identifier
     * @param q  the minimum amount of resources to allocate to each VM. >= 0
     */
    public Preserve(VM vm, String r, int q) {
        super(Collections.singleton(vm), Collections.<Node>emptySet(), false);
        if (q < 0) {
            throw new IllegalArgumentException("The amount of resource must be >= 0");
        }
        this.rc = r;
        this.amount = q;
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
        return super.equals(o) && rc.equals(((Preserve) o).rc) && amount == ((Preserve) o).amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rc, amount);
    }

    @Override
    public String toString() {
        return "preserve(vm=" + getInvolvedVMs().iterator().next() +
                ", rc=" + rc +
                ", amount=" + amount +
                ", discrete" + ')';
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }

    @Override
    public SatConstraintChecker<Preserve> getChecker() {
        return new PreserveChecker(this);
    }

}


