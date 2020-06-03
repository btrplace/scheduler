/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.VM;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A constraint to force several sets of VMs to not share any node when they are
 * running.
 * <p>
 * When the restriction is discrete, the constraint ensures there is no co-location on
 * only on a given model.
 * <p>
 * When the restriction is continuous, the constraint ensures a VM can not be set running
 * on a node that is hosting VMs from another group, even temporary.
 *
 * @author Fabien Hermenier
 */

@SideConstraint(args = {"part <<: vms"}, inv = "{ {host(v). v : p , vmState(v) = running}. p : part} <<: nodes")
public class Split extends SimpleConstraint {

  private final Collection<Collection<VM>> sets;

    /**
     * Make a new constraint having a discrete restriction.
     *
     * @param parts the disjoint sets of VMs that must be split
     */
    public Split(Collection<Collection<VM>> parts) {
        this(parts, false);
    }

    /**
     * Make a new constraint.
     *
     * @param parts      the disjoint sets of VMs that must be split
     * @param continuous {@code true} for a continuous restriction
     */
    public Split(Collection<Collection<VM>> parts, boolean continuous) {
        super(continuous);
        Set<VM> all = new HashSet<>();
        int cnt = 0;
        for (Collection<VM> s : parts) {
            cnt += s.size();
            all.addAll(s);
            if (cnt != all.size()) {
                throw new IllegalArgumentException("The constraint expects disjoint sets of VMs");
            }
        }

        this.sets = parts;
    }


    @Override
    public Set<VM> getInvolvedVMs() {
        Set<VM> s = new HashSet<>();
        sets.forEach(s::addAll);
        return s;
    }

    /**
     * Get the groups of VMs identifier.
     *
     * @return the groups
     */
    public Collection<Collection<VM>> getSets() {
        return this.sets;
    }

    /**
     * Get the group of VMs that contains the given VM.
     *
     * @param u the VM identifier
     * @return the group of VM if exists, an empty collection otherwise
     */
    public Collection<VM> getAssociatedVGroup(VM u) {
        for (Collection<VM> vGrp : sets) {
            if (vGrp.contains(u)) {
                return vGrp;
            }
        }
        return Collections.emptySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Split split = (Split) o;
        return isContinuous() == split.isContinuous() &&
                Objects.equals(sets, split.sets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sets, isContinuous());
    }

    @Override
    public String toString() {
        return "split(vms=" + sets + ", " + (isContinuous() ? "continuous" : "discrete") + ')';
    }

    @Override
    public SplitChecker getChecker() {
        return new SplitChecker(this);
    }

}
