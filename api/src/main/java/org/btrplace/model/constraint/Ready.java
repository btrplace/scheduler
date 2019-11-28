/*
 * Copyright (c) 2019 University Nice Sophia Antipolis
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

    private VM vm;

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
