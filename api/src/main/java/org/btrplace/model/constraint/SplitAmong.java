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
 * A constraint to force sets of running VMs to be hosted on distinct set of nodes.
 * VMs inside a same set may still be collocated.
 * <p>
 * The set of VMs must be disjoint so must be the set of servers.
 * <p>
 * If the constraint is set to provide a discrete restriction, it only ensures no group of VMs share a group of nodes
 * while each group of VMs does not spread over several group of nodes. This allows to change the group of nodes
 * hosting the group of VMs during the reconfiguration process.
 * <p>
 * If the constraint is set to provide a continuous restriction, the constraint must be satisfied initially, then the VMs
 * of a single group can never spread on multiple groups of nodes nor change of group.
 *
 * @author Fabien Hermenier
 */
public class SplitAmong extends SatConstraint {

    /**
     * Set of set of vms.
     */
    private Collection<Collection<VM>> vGroups;

    /**
     * Set of set of nodes.
     */
    private Collection<Collection<Node>> pGroups;

    /**
     * Make a new constraint having a discrete restriction.
     *
     * @param vParts the disjoint sets of VMs
     * @param pParts the disjoint sets of nodes.
     */
    public SplitAmong(Collection<Collection<VM>> vParts, Collection<Collection<Node>> pParts) {
        this(vParts, pParts, false);
    }


    /**
     * Make a new constraint.
     *
     * @param vParts     the disjoint sets of VMs
     * @param pParts     the disjoint sets of nodes.
     * @param continuous {@code true} for a continuous restriction
     */
    public SplitAmong(Collection<Collection<VM>> vParts, Collection<Collection<Node>> pParts, boolean continuous) {
        super(null, null, continuous);
        int cnt = 0;
        Set<Node> all = new HashSet<>();
        for (Collection<Node> s : pParts) {
            cnt += s.size();
            all.addAll(s);
            if (cnt != all.size()) {
                throw new IllegalArgumentException("The constraint expects disjoint sets of nodes");
            }
        }

        this.vGroups = vParts;
        this.pGroups = pParts;
    }

    @Override
    public Set<VM> getInvolvedVMs() {
        Set<VM> s = new HashSet<>();
        for (Collection<VM> x : vGroups) {
            s.addAll(x);
        }
        return s;
    }

    @Override
    public Set<Node> getInvolvedNodes() {
        Set<Node> s = new HashSet<>();
        for (Collection<Node> x : pGroups) {
            s.addAll(x);
        }
        return s;
    }

    /**
     * Get the groups of VMs identifiers
     *
     * @return the groups
     */
    public Collection<Collection<VM>> getGroupsOfVMs() {
        return vGroups;
    }

    /**
     * Get the groups of nodes identifiers
     *
     * @return the groups
     */
    public Collection<Collection<Node>> getGroupsOfNodes() {
        return pGroups;
    }

    /**
     * Get the group of nodes associated to a given node.
     *
     * @param u the node
     * @return the associated group of nodes if exists, {@code null} otherwise
     */
    public Collection<Node> getAssociatedPGroup(Node u) {
        for (Collection<Node> pGrp : pGroups) {
            if (pGrp.contains(u)) {
                return pGrp;
            }
        }
        return null;
    }

    /**
     * Get the group of VMs associated to a given VM.
     *
     * @param u the VM
     * @return the associated group of VMs if exists, {@code null} otherwise
     */
    public Collection<VM> getAssociatedVGroup(VM u) {
        for (Collection<VM> vGrp : vGroups) {
            if (vGrp.contains(u)) {
                return vGrp;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SplitAmong that = (SplitAmong) o;

        return pGroups.equals(that.pGroups) && vGroups.equals(that.vGroups) && this.isContinuous() == that.isContinuous();
    }

    @Override
    public int hashCode() {
        return Objects.hash(vGroups, pGroups, isContinuous());
    }

    @Override
    public String toString() {
        return "splitAmong(" + "vms=[" + vGroups + ", nodes=" + pGroups + ", " + restrictionToString() + ')';
    }


    @Override
    public SatConstraintChecker<SplitAmong> getChecker() {
        return new SplitAmongChecker(this);
    }

}
