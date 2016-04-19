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
 * A constraint to force a VM at being running.
 *
 * @author Fabien Hermenier
 */
public class Running implements SatConstraint {

    private VM vm;

    private boolean continuous;

    /**
     * Make a new constraint.
     *
     * @param vm the VM to make running
     */
    public Running(VM vm) {
        this(vm, false);
    }

    /**
     * Make a new constraint.
     *
     * @param vm         the VM to make running
     * @param continuous {@code true} for a continuous restriction
     */
    public Running(VM vm, boolean continuous) {
        this.vm = vm;
        this.continuous = continuous;
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
    public boolean isContinuous() {
        return continuous;
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
        Running running = (Running) o;
        return continuous == running.continuous &&
                Objects.equals(vm, running.vm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vm, continuous);
    }

    @Override
    public RunningChecker getChecker() {
        return new RunningChecker(this);
    }

    @Override
    public String toString() {
        return "running(vms=" + vm + ", " + (continuous ? "continuous" : "discrete") + ")";
    }

    /**
     * Instantiate discrete constraints for a collection of VMs.
     *
     * @param vms the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<Running> newRunning(Collection<VM> vms) {
        List<Running> l = new ArrayList<>(vms.size());
        for (VM v : vms) {
            l.add(new Running(v));
        }
        return l;
    }

}
