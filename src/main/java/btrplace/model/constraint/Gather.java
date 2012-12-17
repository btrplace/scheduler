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
import java.util.Set;
import java.util.UUID;

/**
 * A constraint to force a set of VMs, if running, to be
 * hosted on the same node.
 * <p/>
 * The restriction provided by the constraint is discrete.
 * VMs may then be temporary not co-located during the reconfiguration process.
 * To disallow that, a {@link Root} constraint may be necessary but in this
 * setting, no relocation will be possible at all.
 *
 * @author Fabien Hermenier
 */
public class Gather extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param vms the VMs to group
     */
    public Gather(Set<UUID> vms) {
        super(vms, Collections.<UUID>emptySet(), false);
    }


    @Override
    public Sat isSatisfied(Model i) {
        UUID used = null;
        Mapping map = i.getMapping();
        for (UUID vm : getInvolvedVMs()) {
            if (map.getRunningVMs().contains(vm)) {
                if (used == null) {
                    used = map.getVMLocation(vm);
                } else if (!used.equals(map.getVMLocation(vm))) {
                    return Sat.UNSATISFIED;
                }
            }
        }
        return Sat.SATISFIED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Gather that = (Gather) o;
        return getInvolvedVMs().equals(that.getInvolvedVMs());
    }

    @Override
    public int hashCode() {
        return getInvolvedVMs().hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder("gather(")
                .append("vms=").append(getInvolvedVMs())
                .append(", discrete")
                .append(")").toString();
    }

    @Override
    public boolean setContinuous(boolean b) {
        if (!b) {
            super.setContinuous(b);
        }
        return !b;
    }
}
