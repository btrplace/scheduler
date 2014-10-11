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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A constraint to avoid VM relocation to another host.
 * <p>
 * The restriction provided by the constraint is only continuous. The running
 * VMs will stay on their current node for the whole duration of the reconfiguration
 * process.
 *
 * @author Fabien Hermenier
 */
public class Root extends SatConstraint {

    /**
     * Instantiate constraints for a collection of VMs.
     *
     * @param vms the VMs to integrate
     * @return the associated list of constraints
     */
    public static List<Root> newRoots(Collection<VM> vms) {
        List<Root> l = new ArrayList<>(vms.size());
        for (VM v : vms) {
            l.add(new Root(v));
        }
        return l;
    }

    /**
     * Make a new constraint.
     *
     * @param vm the VM to disallow to move
     */
    public Root(VM vm) {
        super(Collections.singleton(vm), Collections.<Node>emptySet(), true);
    }

    @Override
    public String toString() {
        return "root(" + "vm=" + getInvolvedVMs().iterator().next() + ", continuous" + ")";
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public SatConstraintChecker<Root> getChecker() {
        return new RootChecker(this);
    }

}
