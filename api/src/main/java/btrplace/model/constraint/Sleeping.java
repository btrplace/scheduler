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

import btrplace.model.constraint.checker.SatConstraintChecker;
import btrplace.model.constraint.checker.SleepingChecker;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * A constraint to force a set of VMs at being sleeping.
 * <p/>
 * The restriction provided by the constraint is discrete
 * however, if some of the VMs are already sleeping, then
 * their state will be unchanged.
 *
 * @author Fabien Hermenier
 */
public class Sleeping extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param vms the VMs to make sleeping
     */
    public Sleeping(Set<UUID> vms) {
        super(vms, Collections.<UUID>emptySet(), false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Sleeping that = (Sleeping) o;
        return getInvolvedVMs().equals(that.getInvolvedVMs());
    }

    @Override
    public int hashCode() {
        return getInvolvedVMs().hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder("sleeping(")
                .append("vms=").append(getInvolvedVMs())
                .append(", discrete")
                .append(")").toString();
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new SleepingChecker(this);
    }

}
