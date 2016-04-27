/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.model.constraint.migration;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraint;

import java.util.*;

/**
 * A constraint to ensure no overlapping between a set of migrations.
 *
 * @author Vincent Kherbache
 */
public class Serialize implements SatConstraint {

    private Set<VM> vms;

    /**
     * Make a new constraint.
     *
     * @param vms a list of at least 2 VMs to serialize
     */
    public Serialize(Set<VM> vms) {
        this.vms = vms;
    }

    /**
     * Make a new constraint.
     *
     * @param vm1 the first VM to serialize
     * @param vm2 the second VM to serialize
     */
    public Serialize(VM vm1, VM vm2) {
        this(new HashSet<>(Arrays.asList(vm1, vm2)));
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public SerializeChecker getChecker() {
        return new SerializeChecker(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Serialize serialize = (Serialize) o;
        return Objects.equals(vms, serialize.vms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vms);
    }

    @Override
    public Collection<Node> getInvolvedNodes() {
        return Collections.emptyList();
    }

    @Override
    public Set<VM> getInvolvedVMs() {
        return vms;
    }

    @Override
    public boolean isContinuous() {
        return false;
    }

    @Override
    public String toString() {
        return "serialize(vms=" + getInvolvedVMs() + ", continuous)";
    }
}
