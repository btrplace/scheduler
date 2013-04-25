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
import btrplace.plan.ReconfigurationPlanValidator;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.DefaultReconfigurationPlanValidator;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ResumeVM;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A constraint to disallow the given VMs, when running,
 * to be hosted on a given set of nodes.
 * <p/>
 * The restriction provided by this constraint is only discrete.
 *
 * @author Fabien Hermenier
 * @see SatConstraint
 */
public class Ban extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param vms   the VMs identifiers
     * @param nodes the nodes identifiers
     */
    public Ban(Set<UUID> vms, Set<UUID> nodes) {
        super(vms, nodes, false);
    }

    @Override
    public Sat isSatisfied(Model i) {
        Mapping c = i.getMapping();
        Set<UUID> runnings = c.getRunningVMs();
        for (UUID vm : getInvolvedVMs()) {
            if (runnings.contains(vm) && getInvolvedNodes().contains(c.getVMLocation(vm))) {
                return Sat.UNSATISFIED;
            }
        }
        return Sat.SATISFIED;
    }

    @Override
    public String toString() {
        return new StringBuilder("ban(")
                .append("vms=").append(getInvolvedVMs())
                .append(", nodes=").append(getInvolvedNodes())
                .append(", discrete)").toString();
    }

    @Override
    public boolean setContinuous(boolean b) {
        if (!b) {
            super.setContinuous(b);
        }
        return !b;
    }

    @Override
    public ReconfigurationPlanValidator getValidator() {
        return new Checker(new HashSet<>(getInvolvedVMs()));
    }

    /**
     * Checker for the constraint.
     */
    private class Checker extends DefaultReconfigurationPlanValidator {

        public Checker(Set<UUID> vms) {
            super(vms);
        }

        @Override
        public boolean accept(BootVM a) {
            return !onDenied(a.getVM(), a.getDestinationNode());
        }

        private boolean onDenied(UUID vm, UUID n) {
            return isTracked(vm) && getInvolvedNodes().contains(n);
        }

        @Override
        public boolean accept(MigrateVM a) {
            return !onDenied(a.getVM(), a.getDestinationNode());
        }

        @Override
        public boolean accept(ResumeVM a) {
            return !onDenied(a.getVM(), a.getDestinationNode());
        }

        @Override
        public boolean acceptOriginModel(Model mo) {
            return true;
        }

        @Override
        public boolean acceptResultingModel(Model mo) {
            Mapping c = mo.getMapping();
            Set<UUID> runnings = c.getRunningVMs();
            for (UUID vm : getTrackedVMs()) {
                if (runnings.contains(vm) && getInvolvedNodes().contains(c.getVMLocation(vm))) {
                    return false;
                }
            }
            return true;
        }
    }
}
