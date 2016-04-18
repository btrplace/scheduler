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

import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.*;

/**
 * A constraint to force a VM at being sleeping.
 * <p>
 *
 * @author Fabien Hermenier
 */
public class Sleeping implements SatConstraint {

    private VM vm;

    private boolean continuous;

    /**
     * Make a new discrete constraint.
     *
     * @param vm the VMs to make sleeping
     */
    public Sleeping(VM vm) {
        this(vm, false);
    }

    /**
     * Make a new discrete constraint.
     *
     * @param vm         the VMs to make sleeping
     * @param continuous {@code true} for a continuous restriction
     */
    public Sleeping(VM vm, boolean continuous) {
        this.vm = vm;
        this.continuous = continuous;
    }


    @Override
    public SleepingChecker getChecker() {
        return new SleepingChecker(this);
    }

    @Override
    public String toString() {
        return "sleeping(vms=" + vm + ", " + (isContinuous() ? "continuous" : "discrete") + ")";
    }

    @Override
    public boolean isContinuous() {
        return continuous;
    }

    @Override
    public boolean setContinuous(boolean b) {
        continuous = b;
        return true;
    }

    @Override
    public Collection<Node> getInvolvedNodes() {
        return Collections.emptyList();
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
        return continuous == sleeping.continuous &&
                Objects.equals(vm, sleeping.vm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vm, continuous);
    }

    /**
     * Instantiate discrete constraints for a collection of VMs.
     *
     * @param vms the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<Sleeping> newSleeping(Collection<VM> vms) {
        List<Sleeping> l = new ArrayList<>(vms.size());
        for (VM v : vms) {
            l.add(new Sleeping(v));
        }
        return l;
    }
}
