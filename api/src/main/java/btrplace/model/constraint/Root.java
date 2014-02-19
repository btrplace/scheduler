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
import btrplace.model.constraint.checker.RootChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A constraint to avoid VM relocation to another host.
 * <p/>
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Root that = (Root) o;
        return getInvolvedVMs().equals(that.getInvolvedVMs());
    }

    @Override
    public int hashCode() {
        return getInvolvedVMs().hashCode();
    }

    @Override
    public String toString() {
        return "root(" + "vm=" + getInvolvedVMs().iterator().next() + ", continuous" + ")";
    }

    @Override
    public boolean setContinuous(boolean b) {
        if (b) {
            return super.setContinuous(b);
        }
        return b;
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new RootChecker(this);
    }

}
