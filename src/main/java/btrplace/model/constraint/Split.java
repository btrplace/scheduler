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
 * A constraint to force several set of VMs to not share any node when they are
 * running.
 * <p/>
 * The restriction provided by the constraint is discrete.
 * TODO: Possible to have a continuous restriction ?
 *
 * @author Fabien Hermenier
 */
public class Split extends SatConstraint {

    private Collection<Set<UUID>> sets;

    /**
     * Make a new constraint.
     *
     * @param sets the disjoint sets of VMs that must be split
     */
    public Split(Collection<Set<UUID>> sets) {
        super(null, Collections.<UUID>emptySet(), false);
        this.sets = sets;
    }

    @Override
    public Collection<UUID> getInvolvedVMs() {
        Set<UUID> s = new HashSet<UUID>();
        for (Set<UUID> set : sets) {
            s.addAll(set);
        }
        return s;
    }

    /**
     * Get the groups of VMs identifier.
     *
     * @return the groups
     */
    public Collection<Set<UUID>> getSets() {
        return this.sets;
    }

    @Override
    public Sat isSatisfied(Model i) {
        Mapping m = i.getMapping();
        List<Set<UUID>> used = new ArrayList<Set<UUID>>(sets.size()); //The pgroups that are used
        for (Set<UUID> vgrp : sets) {
            Set<UUID> myGroup = new HashSet<UUID>();

            //Get the servers used by this group of VMs
            for (UUID vmId : vgrp) {
                if (m.getRunningVMs().contains(vmId)) {
                    UUID nId = m.getVMLocation(vmId);
                    //Is this server inside another group ?
                    for (Set<UUID> pGroup : used) {
                        if (pGroup.contains(nId)) {
                            return Sat.UNSATISFIED;
                        }
                    }
                    myGroup.add(nId);
                }
            }
            used.add(myGroup);
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

        Split that = (Split) o;
        return sets.equals(that.sets);
    }

    @Override
    public int hashCode() {
        return sets.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("split");
        for (Iterator<Set<UUID>> ite = sets.iterator(); ite.hasNext(); ) {
            b.append("vms=").append(ite.next());
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
