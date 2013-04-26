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
import btrplace.plan.DenyMyVMsActions;
import btrplace.plan.ReconfigurationPlanValidator;
import btrplace.plan.event.ForgeVM;
import btrplace.plan.event.ShutdownVM;
import btrplace.plan.event.VMStateChangeValidator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A constraint to force a set of VMs at being ready for running.
 * <p/>
 * The restriction provided by the constraint is discrete
 * however, if some of the VMs are already in the ready state, then
 * their state will be unchanged.
 *
 * @author Fabien Hermenier
 */
public class Ready extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param vms the VMs to make ready
     */
    public Ready(Set<UUID> vms) {
        super(vms, Collections.<UUID>emptySet(), false);
    }

    @Override
    public Sat isSatisfied(Model i) {
        Mapping c = i.getMapping();
        for (UUID vm : getInvolvedVMs()) {
            if (!c.getReadyVMs().contains(vm)) {
                return Sat.UNSATISFIED;
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
        Ready that = (Ready) o;
        return getInvolvedVMs().equals(that.getInvolvedVMs());
    }

    @Override
    public int hashCode() {
        return getInvolvedVMs().hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder("ready(")
                .append("vms=").append(getInvolvedVMs())
                .append(", discrete")
                .append(")").toString();
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }

    @Override
    public ReconfigurationPlanValidator getValidator() {
        return new Checker(new HashSet<>(getInvolvedVMs()));
    }

    private class Checker extends VMStateChangeValidator {

        public Checker(Set<UUID> vms) {
            super(vms);
        }

        @Override
        public boolean accept(ForgeVM a) {
            return true;
        }

        @Override
        public boolean accept(ShutdownVM a) {
            return true;
        }

        @Override
        public boolean acceptResultingModel(Model mo) {
            Mapping c = mo.getMapping();
            for (UUID vm : getInvolvedVMs()) {
                if (!c.getReadyVMs().contains(vm)) {
                    return false;
                }
            }
            return true;
        }
    }

    private class Checker2 extends DenyMyVMsActions {

        public Checker2(Set<UUID> vs, Set<UUID> ns) {
            super(vs, ns);
        }

        @Override
        public boolean start(ForgeVM a) {
            return true;
        }

        @Override
        public boolean start(ShutdownVM a) {
            return true;
        }

        @Override
        public boolean endsWith(Model mo) {
            Mapping c = mo.getMapping();
            for (UUID vm : vms) {
                if (!c.getReadyVMs().contains(vm)) {
                    return false;
                }
            }
            return true;
        }
    }
}
