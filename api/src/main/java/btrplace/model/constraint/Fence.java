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
import btrplace.model.constraint.checker.DefaultSatConstraintChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;
import btrplace.plan.RunningVMPlacement;

import java.util.Set;
import java.util.UUID;

/**
 * A constraint to force the given VMs, when running,
 * to be hosted on a given group of nodes.
 * <p/>
 * The restriction provided by this constraint is discrete.
 *
 * @author Fabien Hermenier
 */
public class Fence extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param vms   the VMs identifiers
     * @param nodes the nodes identifiers
     */
    public Fence(Set<UUID> vms, Set<UUID> nodes) {
        super(vms, nodes, false);
    }

    @Override
    public String toString() {
        return new StringBuilder("fence(vms=")
                .append(getInvolvedVMs())
                .append(", nodes=").append(getInvolvedNodes())
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

    @Override
    public SatConstraintChecker getChecker() {
        return new Checker(this);
    }

    private class Checker extends DefaultSatConstraintChecker {

        public Checker(Fence f) {
            super(f);
        }

        @Override
        public boolean startRunningVMPlacement(RunningVMPlacement r) {
            if (vms.contains(r.getVM())) {
                return nodes.contains(r.getDestinationNode());
            }
            return true;
        }

        @Override
        public boolean endsWith(Model mo) {
            Mapping c = mo.getMapping();
            Set<UUID> runnings = c.getRunningVMs();
            for (UUID vm : vms) {
                if (runnings.contains(vm) && !nodes.contains(c.getVMLocation(vm))) {
                    return false;
                }
            }
            return true;
        }
    }
}
