/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint.migration;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.SideConstraint;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * A constraint to force some vms migration to terminate or begin (depending of the migration algorithm)
 * at the same time.
 *
 * @author Vincent Kherbache
 */
@SideConstraint(args = {"vs <: vms"}, inv = "!(v : vs) !(a,b : actions(v)) begin(a) = begin(b)")
public class Sync implements SatConstraint {

  private final Collection<VM> vms;

    /**
     * Make a new constraint.
     *
     * @param vms a list of at least 2 VMs to synchronize
     */
    public Sync(Collection<VM> vms) {
        this.vms = vms;
    }

    /**
     * Make a new constraint.
     *
     * @param vm1 the first VM to synchronize
     * @param vm2 the second VM t synchronize
     */
    public Sync(VM vm1, VM vm2) {
        this(Arrays.asList(vm1, vm2));
    }


    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public SyncChecker getChecker() {
        return new SyncChecker(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Sync sync = (Sync) o;
        return Objects.equals(vms, sync.vms);
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
    public Collection<VM> getInvolvedVMs() {
        return vms;
    }

    @Override
    public boolean isContinuous() {
        return false;
    }

    @Override
    public String toString() {
        return "sync(vms=" + vms + ", continuous)";
    }
}
