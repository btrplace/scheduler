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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A constraint to force a set of VMs to be hosted on a single group of nodes
 * among those available.
 * <p>
 * When the restriction is discrete, the constraint only ensure that the VMs are not spread over several
 * group of nodes at the end of the reconfiguration process. However, this situation may occur temporary during
 * the reconfiguration. Basically, this allows to select a new group of nodes for the VMs.
 * <p>
 * When the restriction is continuous, if some VMs are already running, on a group of nodes,
 * it will not be possible to relocated the VMs to a new group of nodes.
 *
 * @author Fabien Hermenier
 */
public class Among extends SatConstraint {

    /**
     * Set of set of nodes.
     */
    private Collection<Collection<Node>> pGroups;


    /**
     * Make a new constraint with a discrete restriction.
     *
     * @param vms   the group of VMs
     * @param parts disjoint set of nodes
     */
    public Among(Collection<VM> vms, Collection<Collection<Node>> parts) {
        this(vms, parts, false);

    }

    /**
     * Make a new constraint.
     *
     * @param vms        the group of VMs
     * @param parts      disjoint set of nodes
     * @param continuous {@code true} for a continuous restriction
     */
    public Among(Collection<VM> vms, Collection<Collection<Node>> parts, boolean continuous) {
        super(vms, null, continuous);
        assert checkDisjoint(parts) : "The constraint expects disjoint sets of nodes";
        this.pGroups = parts;
    }

    private static boolean checkDisjoint(Collection<Collection<Node>> parts) {
        Set<Node> all = new HashSet<>();
        int cnt = 0;
        for (Collection<Node> s : parts) {
            cnt += s.size();
            all.addAll(s);
            if (cnt != all.size()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the group of nodes that contains the given node.
     *
     * @param u the node identifier
     * @return the group of nodes if exists, {@code null} otherwise
     */
    public Collection<Node> getAssociatedPGroup(Node u) {
        for (Collection<Node> pGrp : pGroups) {
            if (pGrp.contains(u)) {
                return pGrp;
            }
        }
        return null;
    }

    @Override
    public Collection<Node> getInvolvedNodes() {
        Set<Node> s = new HashSet<>();
        for (Collection<Node> x : pGroups) {
            s.addAll(x);
        }
        return s;
    }

    /**
     * Get the groups of nodes identifiers
     *
     * @return the groups
     */
    public Collection<Collection<Node>> getGroupsOfNodes() {
        return pGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Among that = (Among) o;

        return pGroups.equals(that.pGroups) &&
                getInvolvedVMs().equals(that.getInvolvedVMs()) &&
                isContinuous() == that.isContinuous();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInvolvedVMs(), getInvolvedNodes(), pGroups, isContinuous(), getInvolvedVMs());
    }

    @Override
    public String toString() {
        return "among(" + "vms=" + getInvolvedVMs() + ", nodes=" + pGroups + ", " + restrictionToString() + ")";
    }

    @Override
    public SatConstraintChecker<Among> getChecker() {
        return new AmongChecker(this);
    }

}
