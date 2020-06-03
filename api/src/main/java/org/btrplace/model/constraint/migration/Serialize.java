/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint.migration;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraint;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A constraint to ensure no overlapping between a set of migrations.
 *
 * @author Vincent Kherbache
 */
public class Serialize implements SatConstraint {

  private final Set<VM> vms;

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
