/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */
package org.btrplace.model.constraint;

import org.btrplace.model.VM;
import org.btrplace.model.view.ResourceRelated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
public class Preserve implements SatConstraint, ResourceRelated {

  private final VM vm;

  private final int amount;

  private final String rc;

    /*
     * Make a new constraint.
     *
     * @param vm the VM
     * @param r  the resource identifier
     * @param q  the minimum amount of resources to allocate to each VM. &gt;= 0
     */
    public Preserve(VM vm, String r, int q) {
        this.vm = vm;
        if (q < 0) {
            throw new IllegalArgumentException("The amount of resource must be >= 0");
        }
        this.rc = r;
        this.amount = q;
    }


    @Override
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
    public String toString() {
        return "preserve(vm=" + vm +
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

    @Override
    public Collection<VM> getInvolvedVMs() {
        return Collections.singleton(vm);
    }

    @Override
    public boolean isContinuous() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Preserve preserve = (Preserve) o;
        return amount == preserve.amount &&
                Objects.equals(vm, preserve.vm) &&
                Objects.equals(rc, preserve.rc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vm, amount, rc);
    }

    /**
     * Make multiple constraints
     *
     * @param vms the VMs involved in the constraints
     * @param r   the resource identifier
     * @param q   the minimum amount of resources to allocate to each VM. Must be positive
     * @return a list of constraints
     */
    public static List<Preserve> newPreserve(Collection<VM> vms, String r, int q) {
        return vms.stream().map(v -> new Preserve(v, r, q)).collect(Collectors.toList());
    }
}


