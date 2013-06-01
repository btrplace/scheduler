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
import btrplace.model.constraint.checker.SplitChecker;

import java.util.*;

/**
 * A constraint to force several sets of VMs to not share any node when they are
 * running.
 * <p/>
 * When the restriction is discrete, the constraint ensures there is no co-location on
 * only on a given model.
 * <p/>
 * When the restriction is continuous, the constraint ensures a VM can not be set running
 * on a node that is hosting VMs from another group, even temporary.
 *
 * @author Fabien Hermenier
 */
public class Split extends SatConstraint {

    private Collection<Set<VM>> sets;

    /**
     * Make a new constraint having a discrete restriction.
     *
     * @param parts the disjoint sets of VMs that must be split
     */
    public Split(Collection<Set<VM>> parts) {
        this(parts, false);
    }

    /**
     * Make a new constraint.
     *
     * @param parts      the disjoint sets of VMs that must be split
     * @param continuous {@code true} for a continuous restriction
     */
    public Split(Collection<Set<VM>> parts, boolean continuous) {
        super(null, Collections.<Node>emptySet(), continuous);
        Set<VM> all = new HashSet<>();
        int cnt = 0;
        for (Set<VM> s : parts) {
            cnt += s.size();
            all.addAll(s);
            if (cnt != all.size()) {
                throw new IllegalArgumentException("The constraint expects disjoint sets of VMs");
            }
        }

        this.sets = parts;
    }


    @Override
    public Collection<VM> getInvolvedVMs() {
        Set<VM> s = new HashSet<>();
        for (Set<VM> set : sets) {
            s.addAll(set);
        }
        return s;
    }

    /**
     * Get the groups of VMs identifier.
     *
     * @return the groups
     */
    public Collection<Set<VM>> getSets() {
        return this.sets;
    }

    /**
     * Get the group of VMs that contains the given VM.
     *
     * @param u the VM identifier
     * @return the group of VM if exists, {@code null} otherwise
     */
    public Set<VM> getAssociatedVGroup(VM u) {
        for (Set<VM> vGrp : sets) {
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
        for (Iterator<Set<VM>> ite = sets.iterator(); ite.hasNext(); ) {
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
        return new SplitChecker(this);
    }

}
