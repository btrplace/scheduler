/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.VM;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A constraint to force a VM at being ready for running.
 * <p>
 * The restriction provided by the constraint is discrete
 * however, if the VM is already in the ready state, then
 * its state will be unchanged.
 *
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"v : vms"}, inv = "vmState(v) = ready")
public class Ready extends SimpleConstraint {

  private final VM vm;

    /**
     * Make a new constraint.
     *
     * @param vm         the VM to make ready
     */
    public Ready(VM vm) {
        super(false);
        this.vm = vm;
    }

    @Override
    public ReadyChecker getChecker() {
        return new ReadyChecker(this);
    }

    @Override
    public String toString() {
        return "ready(vms=" + vm + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Ready ready = (Ready) o;
        return Objects.equals(vm, ready.vm);
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vm);
    }

    @Override
    public Collection<VM> getInvolvedVMs() {
        return Collections.singleton(vm);
    }

    /**
     * Instantiate discrete constraints for a collection of VMs.
     *
     * @param vms the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<Ready> newReady(Collection<VM> vms) {
        return vms.stream().map(Ready::new).collect(Collectors.toList());
    }

    /**
     * Instantiate discrete constraints for a collection of VMs.
     *
     * @param vms the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<Ready> newReady(VM... vms) {
        return newReady(Arrays.asList(vms));
    }
}
