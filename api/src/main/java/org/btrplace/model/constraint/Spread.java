/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.VM;

import java.util.Objects;
import java.util.Set;

/**
 * A constraint to force that the given VMs, if running,
 * to be hosted on distinct nodes.
 * <p>
 * If the restriction is continuous, the constraint ensure no VMs are relocated to a node hosting a VM
 * involved in the same Spread constraint.
 * <p>
 * If the restriction is discrete, the constraint only ensures that there is no co-location
 * at the end of the reconfiguration plan.
 *
 * @author Fabien Hermenier
 */

@SideConstraint(args = {"vs <: vms"}, inv = "!(x,y : vs) (x /= y & vmState(x) = running & vmState(y) = running) --> host(x) /= host(y)")
public class Spread extends SimpleConstraint {

  private final Set<VM> vms;

    /**
     * Make a new constraint having a continuous restriction.
     *
     * @param vms the VMs to consider
     */
    public Spread(Set<VM> vms) {
        this(vms, true);
    }

    /**
     * Make a new constraint.
     *
     * @param vms        the VMs to consider
     * @param continuous {@code true} for a continuous restriction.
     */
    public Spread(Set<VM> vms, boolean continuous) {
        super(continuous);
        this.vms = vms;
    }

    @Override
    public String toString() {
        return "spread(vms=" + getInvolvedVMs() + ", " + (isContinuous() ? "continuous" : "discrete") + ')';
    }

    @Override
    public SpreadChecker getChecker() {
        return new SpreadChecker(this);
    }

    @Override
    public Set<VM> getInvolvedVMs() {
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
        Spread spread = (Spread) o;
        return isContinuous() == spread.isContinuous() &&
                Objects.equals(vms, spread.vms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vms, isContinuous());
    }
}
