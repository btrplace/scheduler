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
 * A constraint to force a VM to be killed.
 *
 * @author Fabien Hermenier
 */
public class Killed implements SatConstraint {

    private VM vm;

    private boolean continuous;

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
        this.vm = vm;
        this.continuous = continuous;
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
    public Collection<Node> getInvolvedNodes() {
        return Collections.emptyList();
    }

    @Override
    public Collection<VM> getInvolvedVMs() {
        return Collections.singleton(vm);
    }

    @Override
    public boolean setContinuous(boolean b) {
        continuous = b;
        return true;
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
        return continuous == killed.continuous &&
                Objects.equals(vm, killed.vm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vm, continuous);
    }

    @Override
    public boolean isContinuous() {
        return continuous;
    }

    /**
     * Instantiate discrete constraints for a collection of VMs.
     *
     * @param vms the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<Killed> newKilled(Collection<VM> vms) {
        List<Killed> l = new ArrayList<>(vms.size());
        for (VM v : vms) {
            l.add(new Killed(v));
        }
        return l;
    }

}
