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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A constraint to force the given VM, when running,
 * to be hosted on a given group of nodes.
 *
 * @author Fabien Hermenier
 */
public class Fence extends SatConstraint {

    /**
     * Instantiate discrete constraints for a collection of VMs.
     *
     * @param vms   the VMs to integrate
     * @param nodes the hosts to disallow
     * @return the associated list of constraints
     */
    public static List<Fence> newFence(Collection<VM> vms, Collection<Node> nodes) {
        List<Fence> l = new ArrayList<>(vms.size());
        for (VM v : vms) {
            l.add(new Fence(v, nodes));
        }
        return l;
    }

    /**
     * Make a new discrete constraint.
     *
     * @param vm    the involved VM
     * @param nodes the involved nodes
     */
    public Fence(VM vm, Collection<Node> nodes) {
        this(vm, nodes, false);
    }

    /**
     * Make a new constraint.
     *
     * @param vm         the VM identifiers
     * @param nodes      the nodes identifiers
     * @param continuous {@code true} for a continuous constraint.
     */
    public Fence(VM vm, Collection<Node> nodes, boolean continuous) {
        super(Collections.singleton(vm), nodes, continuous);
    }

    @Override
    public String toString() {
        return "fence(vm=" + getInvolvedVMs() + ", nodes=" + getInvolvedNodes() + ", " + restrictionToString() + ")";
    }

    @Override
    public SatConstraintChecker<Fence> getChecker() {
        return new FenceChecker(this);
    }

}
