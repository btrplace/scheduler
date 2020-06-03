/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.VM;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * A constraint to force a set of VMs, if running, to be
 * hosted on the same node.
 * <p>
 * If the restriction is discrete, VMs may then be temporary not co-located during the reconfiguration process but they
 * are ensure of being co-located at the end of the reconfiguration.
 * <p>
 * If the restriction is continuous, VMs will always be co-located. In practice, if the VMs are all running, they
 * have to already be co-located and it will not possible to relocate them to avoid a potential temporary separation.
 *
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"vs <: vms"}, inv = "!(x,y : vs) (x /= y & vmState(x) = running & vmState(y) = running) --> host(x) = host(y)")
public class Gather extends SimpleConstraint {

  private final Collection<VM> vms;

    /**
     * Make a new constraint between 2 VMs with a discrete restriction.
     *
     * @param vm1 the first VM
     * @param vm2 the second VM
     */
    public Gather(VM vm1, VM vm2) {
        this(Arrays.asList(vm1, vm2), false);
    }

    /**
     * Make a new constraint with a discrete restriction.
     *
     * @param vms the VMs to group
     */
    public Gather(Collection<VM> vms) {
        this(vms, false);
    }

    /**
     * Make a new constraint.
     *
     * @param vms        the VMs to group
     * @param continuous {@code true} for a continuous restriction
     */
    public Gather(Collection<VM> vms, boolean continuous) {
        super(continuous);
        this.vms = vms;
    }

    @Override
    public String toString() {
        return "gather(" + "vms=" + vms + ", " + (isContinuous() ? "continuous" : "discrete") + ')';
    }

    @Override
    public SatConstraintChecker<Gather> getChecker() {
        return new GatherChecker(this);
    }

    @Override
    public Collection<VM> getInvolvedVMs() {
        return vms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Gather gather = (Gather) o;
        return isContinuous() == gather.isContinuous() &&
                vms.size() == gather.vms.size() &&
                vms.containsAll(gather.vms) &&
                gather.vms.containsAll(vms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vms, isContinuous());
    }
}
