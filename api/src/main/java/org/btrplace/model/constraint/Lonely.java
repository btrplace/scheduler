/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

    private Set<VM> vms;

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
