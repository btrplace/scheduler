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

import btrplace.model.VM;
import btrplace.model.constraint.checker.SatConstraintChecker;
import btrplace.model.constraint.checker.SleepingChecker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A constraint to force a VM at being sleeping.
 * <p/>
 * The restriction provided by the constraint is discrete
 * however, if the VM is already sleeping, then
 * its state will be unchanged.
 *
 * @author Fabien Hermenier
 */
public class Sleeping extends VMStateConstraint {

    /**
     * Instantiate constraints for a collection of VMs.
     *
     * @param vms the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<Sleeping> newSleepings(Collection<VM> vms) {
        List<Sleeping> l = new ArrayList<>(vms.size());
        for (VM v : vms) {
            l.add(new Sleeping(v));
        }
        return l;
    }

    /**
     * Make a new constraint.
     *
     * @param vm the VMs to make sleeping
     */
    public Sleeping(VM vm) {
        super("sleeping", vm);
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new SleepingChecker(this);
    }

}
