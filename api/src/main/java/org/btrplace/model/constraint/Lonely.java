/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.VM;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * A constraint to force all the given VMs, when running,
 * to not share their host with other VMs. Co-location between
 * the VMs given as argument is still possible.
 * <p>
 * If the restriction is discrete, then the constraint ensures the given VMs
 * will not be co-located with other VMs only at the end of the reconfiguration process.
 * <p>
 * If the restriction is continuous, then no co-location is possible during the reconfiguration
 * process.
 *
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"vs <: vms"}, inv = "!(i : vs) vmState(i) = running --> (colocated(i) - {i}) <: vs")
public class Lonely extends SimpleConstraint {

  private final Set<VM> vms;

    /**
     * Make a new constraint with a discrete restriction.
     *
     * @param vms the VMs to consider
     */
    public Lonely(Set<VM> vms) {
        this(vms, false);
    }

    /**
     * Make a new constraint.
     *
     * @param vms        the VMs to consider
     * @param continuous {@code true} for a continuous restriction
     */
    public Lonely(Set<VM> vms, boolean continuous) {
        super(continuous);
        this.vms = vms;
    }

    @Override
    public String toString() {
        return "lonely(" + "vms=" + vms + ", " + (isContinuous() ? "continuous" : "discrete") + ')';
    }

    @Override
    public SatConstraintChecker<Lonely> getChecker() {
        return new LonelyChecker(this);
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
        Lonely lonely = (Lonely) o;
        return isContinuous() == lonely.isContinuous() &&
                Objects.equals(vms, lonely.vms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isContinuous(), vms);
    }
}
