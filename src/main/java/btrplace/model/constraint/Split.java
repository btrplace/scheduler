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
import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.RunningVMPlacement;

import java.util.*;

/**
 * A constraint to force several set of VMs to not share any node when they are
 * running.
 * <p/>
 * When the restriction is discrete, the constraint ensures there is no co-location on
 * only on a given model.
 * When the restriction is continuous, the constraint ensures a VM can not be set running
 * on a node that is hosting VMs from another group.
 * <p/>
 * By default, the restriction provided by the constraint is discrete.
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

    /**
     * Get the group of VMs that contains the given VM.
     *
     * @param u the VM identifier
     * @return the group of VM if exists, {@code null} otherwise
     */
    private Set<UUID> getAssociatedVGroup(UUID u) {
        for (Set<UUID> vGrp : sets) {
            if (vGrp.contains(u)) {
                return vGrp;
            }
        }
        return null;
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
    public Sat isSatisfied(ReconfigurationPlan plan) {
        if (plan.getSize() == 0) {
            return isSatisfied(plan.getOrigin());
        }

        //For each relocation action, we check if the
        //destination node is not hosting a VM from another group
        Model cur = plan.getOrigin().clone();
        for (Action a : plan) {
            if (!a.apply(cur)) {
                return Sat.UNSATISFIED;
            }

            UUID destNode;
            if (a instanceof RunningVMPlacement) {
                RunningVMPlacement ra = (RunningVMPlacement) a;
                destNode = ra.getDestinationNode();
                UUID vm = ra.getVM();
                Set<UUID> vmGrp = getAssociatedVGroup(vm);

                if (vmGrp != null) {
                    //The VM is involved in the constraint, the node must not
                    //run any VM that belong to another group.
                    for (UUID vmOn : cur.getMapping().getRunningVMs(destNode)) {
                        Set<UUID> grp = getAssociatedVGroup(vmOn);
                        if (grp != null && !vmGrp.equals(grp)) {
                            return Sat.UNSATISFIED;
                        }
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

        Split that = (Split) o;
        return sets.equals(that.sets);
    }

    @Override
    public int hashCode() {
        return sets.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("split(vms=[");
        for (Iterator<Set<UUID>> ite = sets.iterator(); ite.hasNext(); ) {
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
}
