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
 * A constraint to force VMs' actions to be executed
 * at the beginning (time t=0), without any delay.
 * <p>
 * @author Vincent Kherbache
 */
@SideConstraint(args = {"v : vms"}, inv = "!(a : actions(v)) begin(a) = 0")
public class NoDelay implements SatConstraint {

  private final VM vm;

    /**
     * Make a new constraint.
     *
     * @param vm the vm to restrict
     */
    public NoDelay(VM vm) {
        this.vm = vm;
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }

    @Override
    public NoDelayChecker getChecker() {
        return new NoDelayChecker(this);
    }

    @Override
    public String toString() {
        return "noDelay(" + "vm=" + vm + ", true)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NoDelay noDelay = (NoDelay) o;
        return Objects.equals(vm, noDelay.vm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vm);
    }

    @Override
    public Collection<VM> getInvolvedVMs() {
        return Collections.singleton(vm);
    }

    @Override
    public boolean isContinuous() {
        return false;
    }

    /**
     * Instantiate constraints for a collection of VMs.
     *
     * @param vms the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<NoDelay> newNoDelay(Collection<VM> vms) {
        return vms.stream().map(NoDelay::new).collect(Collectors.toList());
    }
}
