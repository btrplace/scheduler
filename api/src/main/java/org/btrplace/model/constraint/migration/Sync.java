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

package org.btrplace.model.constraint.migration;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.DefaultSatConstraint;

import java.util.*;

/**
 * A constraint to force some vms migration to terminate or begin (depending of the migration algorithm)
 * at the same time.
 *
 * @author Vincent Kherbache
 */
public class Sync extends DefaultSatConstraint {

    /**
     * Make a new constraint.
     *
     * @param vms   a list of at least 2 VMs to synchronize
     */
    public Sync(Collection<VM> vms) {
        super(vms, Collections.<Node>emptyList(), true);
    }

    /**
     * Make a new constraint.
     *
     * @param vm1   the first VM to synchronize
     * @param vm2   the second VM t synchronize
     * @param vms   possible VMs to synchronize with the two first AND also together
     */
    public Sync(VM vm1, VM vm2, VM... vms) {
        super(makeSingleList(vm1, vm2, vms), Collections.<Node>emptyList(), true);
    }

    /**
     * Create a list of VMs.
     *
     * @param vm1   first VM to add on the list
     * @param vm2   second VM to add on the list
     * @param vms   a table of VMs to add (can be empty)
     * @return  the list
     */
    private static List<VM> makeSingleList(VM vm1, VM vm2, VM... vms) {
        List<VM> vmList = new ArrayList<>();
        vmList.add(vm1);
        vmList.add(vm2);
        if (vms.length > 0) {
            vmList.addAll(Arrays.asList(vms));
        }
        return vmList;
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public SyncChecker getChecker() {
        return new SyncChecker(this);
    }

    @Override
    public String toString() {
        return "sync(" + "vms=" + getInvolvedVMs() + ", " + restrictionToString() + ")";
    }
}
