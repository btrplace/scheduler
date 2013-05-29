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
import btrplace.model.constraint.checker.SplitAmongChecker;

import java.util.*;

/**
 * A constraint to force sets of VMs inside to be hosted on distinct set of servers.
 * VMs inside a same set may still be collocated.
 * <p/>
 * The set of VMs must be disjoint so must be the set of servers.
 * <p/>
 * If the constraint is set to provide a discrete restriction, it only ensures no group of VMs share a group of nodes
 * while each group of VMs does not spread over several group of nodes. This allows to change the group of nodes
 * hosting the group of VMs during the reconfiguration process.
 * <p/>
 * If the constraint is set to provide a continuous restriction, the constraint must be satisfied initially, then the VMs
 * of a single group can never spread on multiple groups of nodes nor change of group.
 *
 * @author Fabien Hermenier
 */
public class SplitAmong extends SatConstraint {

    /**
     * Set of set of vms.
     */
    private Set<Set<VM>> vGrps;

    /**
     * Set of set of nodes.
     */
    private Set<Set<Node>> pGrps;

    /**
     * Make a new constraint having a discrete restriction.
     *
     * @param vGrps the set of set of VMs. Sets of VMs must be disjoint
     * @param pGrps the set of set of nodes. Sets of nodes must be disjoint
     */
    public SplitAmong(Set<Set<VM>> vGrps, Set<Set<Node>> pGrps) {
        this(vGrps, pGrps, false);
    }


    /**
     * Make a new constraint.
     *
     * @param vGrps      the set of set of VMs. Sets of VMs must be disjoint
     * @param pGrps      the set of set of nodes. Sets of nodes must be disjoint
     * @param continuous {@code true} for a continuous restriction
     */
    public SplitAmong(Set<Set<VM>> vGrps, Set<Set<Node>> pGrps, boolean continuous) {
        super(null, null, continuous);
        this.vGrps = vGrps;
        this.pGrps = pGrps;
    }

    @Override
    public Collection<VM> getInvolvedVMs() {
        Set<VM> s = new HashSet<>();
        for (Set<VM> x : vGrps) {
            s.addAll(x);
        }
        return s;
    }

    @Override
    public Collection<Node> getInvolvedNodes() {
        Set<Node> s = new HashSet<>();
        for (Set<Node> x : pGrps) {
            s.addAll(x);
        }
        return s;
    }

    /**
     * Get the groups of VMs identifiers
     *
     * @return the groups
     */
    public Set<Set<VM>> getGroupsOfVMs() {
        return vGrps;
    }

    /**
     * Get the groups of nodes identifiers
     *
     * @return the groups
     */
    public Set<Set<Node>> getGroupsOfNodes() {
        return pGrps;
    }

    /**
     * Get the group of nodes associated to a given node.
     *
     * @param u the node
     * @return the associated group of nodes if exists, {@code null} otherwise
     */
    public Set<Node> getAssociatedPGroup(Node u) {
        for (Set<Node> pGrp : pGrps) {
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
    public Set<VM> getAssociatedVGroup(VM u) {
        for (Set<VM> vGrp : vGrps) {
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

        return pGrps.equals(that.pGrps) && vGrps.equals(that.vGrps) && this.isContinuous() == that.isContinuous();
    }

    @Override
    public int hashCode() {
        return Objects.hash(vGrps, pGrps, isContinuous());
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("splitAmong(");
        b.append("vms=[");
        for (Iterator<Set<VM>> ite = vGrps.iterator(); ite.hasNext(); ) {
            b.append(ite.next());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        b.append("], nodes=[");
        for (Iterator<Set<Node>> ite = pGrps.iterator(); ite.hasNext(); ) {
            b.append(ite.next());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        b.append(']');
        if (isContinuous()) {
            b.append(", continuous");
        } else {
            b.append(", discrete");
        }

        return b.append(')').toString();
    }


    @Override
    public SatConstraintChecker getChecker() {
        return new SplitAmongChecker(this);
    }

}
