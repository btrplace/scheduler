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

import btrplace.model.constraint.checker.AmongChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;

import java.util.*;

/**
 * A constraint to force a set of VMs to be hosted on a single group of nodes
 * among those available.
 * <p/>
 * When the restriction is discrete, the constraint only ensure that the VMs are not spread over several
 * group of nodes at the end of the reconfiguration process. However, this situation may occur temporary during
 * the reconfiguration. Basically, this allows to select a new group of nodes for the VMs.
 * <p/>
 * When the restriction is continuous, if some VMs are already running, on a group of nodes,
 * it will not be possible to relocated the VMs to a new group of nodes.
 *
 * @author Fabien Hermenier
 */
public class Among extends SatConstraint {

    /**
     * Set of set of nodes.
     */
    private Set<Set<Integer>> pGrps;


    /**
     * Make a new constraint with a discrete restriction.
     *
     * @param vms       the group of VMs
     * @param phyGroups the candidate group of nodes.
     */
    public Among(Set<Integer> vms, Set<Set<Integer>> phyGroups) {
        this(vms, phyGroups, false);

    }

    /**
     * Make a new constraint.
     *
     * @param vms        the group of VMs
     * @param phyGroups  the candidate group of nodes.
     * @param continuous {@code true} for a continuous restriction
     */
    public Among(Set<Integer> vms, Set<Set<Integer>> phyGroups, boolean continuous) {
        super(vms, null, continuous);
        this.pGrps = phyGroups;
    }

    /**
     * Get the group of nodes that contains the given node.
     *
     * @param u the node identifier
     * @return the group of nodes if exists, {@code null} otherwise
     */
    public Set<Integer> getAssociatedPGroup(int u) {
        for (Set<Integer> pGrp : pGrps) {
            if (pGrp.contains(u)) {
                return pGrp;
            }
        }
        return null;
    }

    @Override
    public Collection<Integer> getInvolvedNodes() {
        Set<Integer> s = new HashSet<>();
        for (Set<Integer> x : pGrps) {
            s.addAll(x);
        }
        return s;
    }

    /**
     * Get the groups of nodes identifiers
     *
     * @return the groups
     */
    public Set<Set<Integer>> getGroupsOfNodes() {
        return pGrps;
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

        return pGrps.equals(that.pGrps) &&
                getInvolvedVMs().equals(that.getInvolvedVMs()) &&
                isContinuous() == that.isContinuous();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInvolvedVMs(), getInvolvedNodes(), pGrps, isContinuous(), getInvolvedVMs());
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("among(");
        b.append("vms=").append(getInvolvedVMs());
        b.append(", nodes=[");
        for (Iterator<Set<Integer>> ite = pGrps.iterator(); ite.hasNext(); ) {
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
        return b.append(")").toString();
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new AmongChecker(this);
    }

}
