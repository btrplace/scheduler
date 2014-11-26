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

<<<<<<< HEAD:api/src/main/java/btrplace/model/constraint/Gather.java
import btrplace.SideConstraint;
import btrplace.model.Node;
import btrplace.model.VM;
=======
import org.btrplace.model.Node;
import org.btrplace.model.VM;
>>>>>>> master:api/src/main/java/org/btrplace/model/constraint/Gather.java

import java.util.Collection;
import java.util.Collections;

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
public class Gather extends SatConstraint {

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
        super(vms, Collections.<Node>emptySet(), continuous);
    }

    @Override
    public String toString() {
        return "gather(" + "vms=" + getInvolvedVMs() + ", " + restrictionToString() + ')';
    }

    @Override
    public SatConstraintChecker<Gather> getChecker() {
        return new GatherChecker(this);
    }

}
