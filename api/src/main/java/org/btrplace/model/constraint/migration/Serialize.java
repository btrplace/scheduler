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
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.SatConstraintChecker;

import java.util.*;

/**
 * A constraint to ensure no overlapping between a set of migrations.
 *
 * @author Vincent Kherbache
 */
public class Serialize extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param vms   a list of at least 2 VMs to serialize
     */
    public Serialize(Collection<VM> vms) {
        super(vms, Collections.<Node>emptyList(), true);
    }

    /**
     * Make a new constraint.
     *
     * @param vm1   the first VM to serialize
     * @param vm2   the second VM to serialize
     * @param vms   possible VMs to serialize with the two first AND also together
     */
    public Serialize(VM vm1, VM vm2, VM... vms) {
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
        if (vms.length > 0) { vmList.addAll(Arrays.asList(vms)); }
        return vmList;
    }
    
    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new SerializeChecker(this);
    }

    @Override
    public String toString() {
        return "serialize(" + "vms=" + getInvolvedVMs() + ", " + restrictionToString() + ")";
    }
}
