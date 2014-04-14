/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.model.constraint;

import btrplace.model.Node;
import btrplace.model.VM;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A constraint to force a VM at being running.
 * <p/>
 * The restriction provided by the constraint is discrete.
 * however, if the VM is already running, then
 * its state will be unchanged.
 *
 * @author Fabien Hermenier
 */
public class Running extends SatConstraint {

    /**
     * Instantiate constraints for a collection of VMs.
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

    /**
     * Make a new constraint.
     *
     * @param vm the VM to make running
     */
    public Running(VM vm) {
        super(Collections.singleton(vm), Collections.<Node>emptySet(), false);
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new RunningChecker(this);
    }

    @Override
    public String toString() {
        return "running(vms=" + getInvolvedVMs().iterator().next() + ", discrete)";
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }
}
