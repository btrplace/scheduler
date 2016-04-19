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

import java.util.Collection;
import java.util.Collections;
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
public class Gather implements SatConstraint {

    private Collection<VM> vms;

    private boolean continuous;

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
        this.vms = vms;
        this.continuous = continuous;
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
    public Collection<Node> getInvolvedNodes() {
        return Collections.emptyList();
    }

    @Override
    public boolean setContinuous(boolean b) {
        continuous = b;
        return true;
    }

    @Override
    public boolean isContinuous() {
        return continuous;
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
        return continuous == gather.continuous &&
                Objects.equals(vms, gather.vms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vms, continuous);
    }
}
