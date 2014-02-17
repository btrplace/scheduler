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
import btrplace.model.constraint.checker.BanChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A constraint to disallow the given VM, when running,
 * to be hosted on a given set of nodes.
 * <p/>
 * The restriction provided by this constraint is only discrete.
 *
 * @author Fabien Hermenier
 * @see SatConstraint
 */
public class Ban extends SatConstraint {

    /**
     * Instantiate constraints for a collection of VMs.
     *
     * @param vms   the VMs to integrate
     * @param nodes the hosts to disallow
     * @return the associated list of constraints
     */
    public static List<Ban> newBans(Collection<VM> vms, Collection<Node> nodes) {
        List<Ban> l = new ArrayList<>(vms.size());
        for (VM v : vms) {
            l.add(new Ban(v, nodes));
        }
        return l;
    }

    /**
     * Make a new constraint.
     *
     * @param vm    the VM identifiers
     * @param nodes the nodes identifiers
     */
    public Ban(VM vm, Collection<Node> nodes) {
        super(Collections.singleton(vm), nodes, false);
    }


    @Override
    public String toString() {
        return new StringBuilder("ban(")
                .append("vm=").append(getInvolvedVMs().iterator().next())
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
    public SatConstraintChecker getChecker() {
        return new BanChecker(this);
    }


}
