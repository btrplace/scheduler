/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;

import java.util.*;

/**
 * A constraint to force sets of VMs inside to be hosted on distinct set of servers.
 * VMs inside a same set may still be collocated.
 * <p/>
 * The set of VMs must be disjoint so must be the set of servers.
 *
 * @author Fabien Hermenier
 */
public class SplitAmong extends SatConstraint {

    /**
     * Set of set of vms.
     */
    private Set<Set<UUID>> vGrps;

    /**
     * Set of set of nodes.
     */
    private Set<Set<UUID>> pGrps;

    /**
     * Make a new constraint.
     *
     * @param vGrps the set of set of VMs. Sets of VMs must be disjoint
     * @param pGrps the set of set of nodes. Sets of nodes must be disjoint
     */
    public SplitAmong(Set<Set<UUID>> vGrps, Set<Set<UUID>> pGrps) {
        super(null, null, false);
        this.vGrps = vGrps;
        this.pGrps = pGrps;
    }

    @Override
    public Collection<UUID> getInvolvedVMs() {
        Set<UUID> s = new HashSet<UUID>();
        for (Set<UUID> x : vGrps) {
            s.addAll(x);
        }
        return s;
    }

    @Override
    public Collection<UUID> getInvolvedNodes() {
        Set<UUID> s = new HashSet<UUID>();
        for (Set<UUID> x : pGrps) {
            s.addAll(x);
        }
        return s;

    }

    /**
     * Get the groups of VMs identifiers
     *
     * @return the groups
     */
    public Set<Set<UUID>> getGroupsOfVMs() {
        return vGrps;
    }

    /**
     * Get the groups of nodes identifiers
     *
     * @return the groups
     */
    public Set<Set<UUID>> getGroupsOfNodes() {
        return pGrps;
    }

    private Set<UUID> getAssociatedPGroup(UUID u) {
        for (Set<UUID> pGrp : pGrps) {
            if (pGrp.contains(u)) {
                return pGrp;
            }
        }
        return null;
    }

    @Override
    public Sat isSatisfied(Model i) {
        Mapping m = i.getMapping();
        Set<Set<UUID>> used = new HashSet<Set<UUID>>(); //The pgroups that are used
        for (Set<UUID> vgrp : vGrps) {
            Set<UUID> choosedGroup = null;

            //Check every running VM in a single vgroup are running in the same pgroup
            for (UUID vmId : vgrp) {
                if (m.getRunningVMs().contains(vmId)) {
                    if (choosedGroup == null) {
                        choosedGroup = getAssociatedPGroup(m.getVMLocation(vmId));
                        if (choosedGroup == null) { //THe VM is running but on an unknown group. It is an error
                            return Sat.UNSATISFIED;
                        } else if (!used.add(choosedGroup)) { //The pgroup has already been used for another set of VMs.
                            return Sat.UNSATISFIED;
                        }
                    } else if (!choosedGroup.contains(vmId)) { //The VM is not in the group with the other
                        return Sat.UNSATISFIED;
                    }
                }
            }
        }
        return Sat.SATISFIED;
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

        return pGrps.equals(that.pGrps) && vGrps.equals(that.vGrps);
    }

    @Override
    public int hashCode() {
        int result = vGrps.hashCode() * 31 + "splitAmong".hashCode();
        result = 31 * result + pGrps.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("splitAmong(");
        for (Iterator<Set<UUID>> ite = vGrps.iterator(); ite.hasNext(); ) {
            b.append("vms=").append(ite.next());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }

        for (Iterator<Set<UUID>> ite = pGrps.iterator(); ite.hasNext(); ) {
            b.append("nodes=").append(ite.next());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }

        return b.toString();
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }
}
