/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.checker.SatConstraintChecker;
import btrplace.model.constraint.checker.SpreadChecker;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * A constraint to force that the given VMs, if running,
 * to be hosted on distinct nodes.
 * <p/>
 * If the restriction is continuous, the constraint ensure no VMs are relocated to a node hosting a VM
 * involved in the same Spread constraint.
 * <p/>
 * If the restriction is discrete, the constraint only ensures that there is no co-location
 * at the end of the reconfiguration plan.
 *
 * @author Fabien Hermenier
 */
public class Spread extends SatConstraint {

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
        super(vms, Collections.<Node>emptySet(), continuous);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("spread(vms=").append(getInvolvedVMs());
        if (!isContinuous()) {
            b.append(", discrete");
        } else {
            b.append(", continuous");
        }
        return b.append(')').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Spread that = (Spread) o;
        return getInvolvedVMs().equals(that.getInvolvedVMs()) && isContinuous() == that.isContinuous();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInvolvedVMs(), isContinuous());
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new SpreadChecker(this);
    }


}
