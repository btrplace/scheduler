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
 * A constraint to force a VM to be killed.
 * The constraint is discrete
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"v : vms"}, inv = "vmState(v) = terminated")
public class Killed extends SimpleConstraint {

  private final VM vm;

    /**
     * Make a new constraint.
     *
     * @param vm         the VMs to remove
     */
    public Killed(VM vm) {
        super(false);
        this.vm = vm;
    }


    @Override
    public KilledChecker getChecker() {
        return new KilledChecker(this);
    }

    @Override
    public String toString() {
        return "killed(vm=" + vm + ")";
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
        Killed killed = (Killed) o;
        return Objects.equals(vm, killed.vm);
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vm);
    }

    /**
     * Instantiate discrete constraints for a collection of VMs.
     *
     * @param vms the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<Killed> newKilled(Collection<VM> vms) {
        return vms.stream().map(Killed::new).collect(Collectors.toList());
    }

}
