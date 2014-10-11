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

import java.util.Collections;
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
        return "spread(vms=" + getInvolvedVMs() + ", " + restrictionToString() + ')';
    }

    @Override
    public SatConstraintChecker<Spread> getChecker() {
        return new SpreadChecker(this);
    }


}
