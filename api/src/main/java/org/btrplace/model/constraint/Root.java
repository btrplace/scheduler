/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.VM;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A constraint to avoid VM relocation to another host.
 * <p>
 * The restriction provided by the constraint is only continuous. The running
 * VMs will stay on their current node for the whole duration of the reconfiguration
 * process.
 *
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"v : vms"}, inv = "vmState(v) = running & ^vmState(v) = running --> ^host(v) = host(v)")
public class Root implements SatConstraint {

  private final VM vm;

    /**
     * Make a new constraint.
     *
     * @param vm the VM to disallow to move
     */
    public Root(VM vm) {
        this.vm = vm;
    }

    @Override
    public String toString() {
        return "root(vm=" + vm + ", continuous)";
    }

    @Override
    public Collection<VM> getInvolvedVMs() {
        return Collections.singleton(vm);
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public RootChecker getChecker() {
        return new RootChecker(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Root root = (Root) o;
        return Objects.equals(vm, root.vm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vm);
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    /**
     * Instantiate constraints for a collection of VMs.
     *
     * @param vms the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<Root> newRoots(Collection<VM> vms) {
        return vms.stream().map(Root::new).collect(Collectors.toList());
    }
}
