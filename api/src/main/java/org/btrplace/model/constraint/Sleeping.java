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

package org.btrplace.model.constraint;

import org.btrplace.model.VM;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A constraint to force a VM at being sleeping.
 * The constraint is discrete
 * <p>
 *
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"v : vms"}, inv = "vmState(v) = sleeping")
public class Sleeping extends SimpleConstraint {

    private VM vm;

    /**
     * Make a new constraint.
     * @param vm         the VM to make sleeping
     */
    public Sleeping(VM vm) {
        super(false);
        this.vm = vm;
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }

    @Override
    public SleepingChecker getChecker() {
        return new SleepingChecker(this);
    }

    @Override
    public String toString() {
        return "sleeping(vms=" + vm + ")";
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
        Sleeping sleeping = (Sleeping) o;
        return Objects.equals(vm, sleeping.vm);
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
    public static List<Sleeping> newSleeping(Collection<VM> vms) {
        return vms.stream().map(Sleeping::new).collect(Collectors.toList());
    }
}
