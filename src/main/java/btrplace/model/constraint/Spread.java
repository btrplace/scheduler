/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A constraint to indicate the given VMs, if running, must be hosted
 * on distinct nodes.
 *
 * @author Fabien Hermenier
 */
public class Spread extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param vms the VMs to consider
     */
    public Spread(Set<UUID> vms) {
        super(vms, Collections.<UUID>emptySet());
    }

    @Override
    public Sat isSatisfied(Model i) {
        Mapping c = i.getMapping();
        Set<UUID> hosts = new HashSet<UUID>();
        for (UUID vm : getInvolvedVMs()) {
            if (c.getRunningVMs().contains(vm) && !hosts.add(c.getVMLocation(vm))) {
                return Sat.UNSATISFIED;
            }
        }
        return Sat.SATISFIED;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("spread(vms=").append(getInvolvedVMs());
        if (!isContinuous()) {
            b.append(", discrete");
        }
        return b.append(")").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Spread that = (Spread) o;
        return getInvolvedVMs().equals(that.getInvolvedVMs());
    }

    @Override
    public int hashCode() {
        return getInvolvedVMs().hashCode();
    }
}
