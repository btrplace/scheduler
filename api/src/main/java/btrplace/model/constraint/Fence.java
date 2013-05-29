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
import btrplace.model.constraint.checker.FenceChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;

import java.util.Set;

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
    public Fence(Set<VM> vms, Set<Node> nodes) {
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
        return new FenceChecker(this);
    }

}
