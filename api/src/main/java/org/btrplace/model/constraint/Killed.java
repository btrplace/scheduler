/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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
 * A constraint to force a VM to be killed.
 *
 * @author Fabien Hermenier
 */
public class Killed extends SimpleConstraint {

    private VM vm;

    /**
     * Make a new discrete constraint.
     *
     * @param vm the VMs to remove
     */
    public Killed(VM vm) {
        this(vm, false);
    }

    /**
     * Make a new constraint.
     *
     * @param vm         the VMs to remove
     * @param continuous {@code true} for a continuous restriction
     */
    public Killed(VM vm, boolean continuous) {
        super(continuous);
        this.vm = vm;
    }


    @Override
    public KilledChecker getChecker() {
        return new KilledChecker(this);
    }

    @Override
    public String toString() {
        return "killed(vm=" + vm + ", " + (isContinuous() ? "continuous" : "discrete") + ")";
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
        return isContinuous() == killed.isContinuous() &&
                Objects.equals(vm, killed.vm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vm, isContinuous());
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
