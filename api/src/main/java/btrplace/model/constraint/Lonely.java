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
import btrplace.model.constraint.checker.LonelyChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;

import java.util.Collections;
import java.util.Set;

/**
 * A constraint to force all the given VMs, when running,
 * to not share their host with other VMs. Co-location between
 * the VMs given as argument is still possible.
 * <p/>
 * If the restriction is discrete, then the constraint ensures the given VMs
 * will not be co-located with other VMs only at the end of the reconfiguration process.
 * <p/>
 * If the restriction is continuous, then no co-location is possible during the reconfiguration
 * process.
 *
 * @author Fabien Hermenier
 */
public class Lonely extends SatConstraint {

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
        super(vms, Collections.<Node>emptySet(), continuous);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("lonely(")
                .append("vms=").append(getInvolvedVMs());

        if (!isContinuous()) {
            b.append(", discrete");
        } else {
            b.append(", continuous");
        }

        return b.append(')').toString();
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new LonelyChecker(this);
    }

}
