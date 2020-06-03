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
 * A constraint to force a VM at being running.
 *
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"v : vms"}, inv = "vmState(v) = running")
public class Running extends SimpleConstraint {

  private final VM vm;

    /**
     * Make a new constraint.
     *
     * @param vm         the VM to make running
     */
    public Running(VM vm) {
        super(false);
        this.vm = vm;
    }

    @Override
    public Collection<VM> getInvolvedVMs() {
        return Collections.singleton(vm);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Running running = (Running) o;
        return Objects.equals(vm, running.vm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vm);
    }

    @Override
    public RunningChecker getChecker() {
        return new RunningChecker(this);
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }

    @Override
    public String toString() {
        return "running(vms=" + vm + ")";
    }

    /**
     * Instantiate discrete constraints for a collection of VMs.
     *
     * @param vms the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<Running> newRunning(Collection<VM> vms) {
        return vms.stream().map(Running::new).collect(Collectors.toList());
    }

    /**
     * Instantiate discrete constraints for a collection of VMs.
     *
     * @param vms the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<Running> newRunning(VM... vms) {
        return newRunning(Arrays.asList(vms));
    }
}
