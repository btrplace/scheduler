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
import btrplace.plan.DefaultReconfigurationPlanChecker;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanChecker;
import btrplace.plan.event.MigrateVM;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * A constraint to avoid relocation. Any running VMs given in parameters
 * will be disallowed to be moved to another host. Other VMs are ignored.
 * <p/>
 * The restriction provided by the constraint is only continuous. The running
 * VMs will stay on their current node for the whole duration of the reconfiguration
 * process.
 *
 * @author Fabien Hermenier
 */
public class Root extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param vms the set of VMs to disallow to move
     */
    public Root(Set<UUID> vms) {
        super(vms, Collections.<UUID>emptySet(), true);
    }

    @Override
    public Sat isSatisfied(ReconfigurationPlan plan) {
        Model r = plan.getResult();
        if (r == null) {
            return Sat.UNSATISFIED;
        }
        Mapping dst = r.getMapping();
        Mapping src = plan.getOrigin().getMapping();
        for (UUID vm : getInvolvedVMs()) {
            if (src.getRunningVMs().contains(vm) && dst.getRunningVMs().contains(vm)
                    && !src.getVMLocation(vm).equals(dst.getVMLocation(vm))) {
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

        Root that = (Root) o;
        return getInvolvedVMs().equals(that.getInvolvedVMs());
    }

    @Override
    public int hashCode() {
        return getInvolvedVMs().hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder("root(")
                .append("vms=").append(getInvolvedVMs())
                .append(", continuous")
                .append(")").toString();
    }

    @Override
    public boolean setContinuous(boolean b) {
        if (b) {
            return super.setContinuous(b);
        }
        return b;
    }

    @Override
    public ReconfigurationPlanChecker getChecker() {
        return new Checker(this);
    }

    /**
     * Checker for the constraint.
     */
    private class Checker extends DefaultReconfigurationPlanChecker {

        public Checker(Root r) {
            super(r);
        }

        @Override
        public boolean start(MigrateVM a) {
            return !vms.contains(a.getVM());
        }
    }

}
