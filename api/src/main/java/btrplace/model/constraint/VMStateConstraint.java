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

import java.util.Collection;
import java.util.Collections;

/**
 * Abstract class for constraints restricting the type of a set of VMs.
 * The constraint is necessarily discrete.
 *
 * @author Fabien Hermenier
 */
public abstract class VMStateConstraint extends SatConstraint {

    private String ref;

    /**
     * Make a new constraint.
     *
     * @param id  the constraint identifier for {@link #toString()}
     * @param vms the VMs to make running
     */
    public VMStateConstraint(String id, Collection<VM> vms) {
        super(vms, Collections.<Node>emptySet(), false);
        this.ref = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return getInvolvedVMs().equals(((VMStateConstraint) o).getInvolvedVMs());
    }

    @Override
    public int hashCode() {
        return getInvolvedVMs().hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder(ref).append('(')
                .append("vms=").append(getInvolvedVMs())
                .append(", discrete")
                .append(')').toString();
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }
}
