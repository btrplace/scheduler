/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A constraint to put a node into quarantine.
 * Running VMs in the quarantine zone can not leave their node
 * while no VMs outside the quarantine zone can be hosted on
 * the node in quarantine.
 * <p>
 * The restriction provided by the constraint is only continuous.
 *
 * @author Fabien Hermenier
 */
public class Quarantine extends SatConstraint {

    /**
     * Instantiate constraints for a collection of nodes.
     *
     * @param nodes the nodes to integrate
     * @return the associated list of constraints
     */
    public static List<Quarantine> newQuarantine(Collection<Node> nodes) {
        List<Quarantine> l = new ArrayList<>(nodes.size());
        for (Node n : nodes) {
            l.add(new Quarantine(n));
        }
        return l;
    }

    /**
     * Make a new constraint.
     *
     * @param n the node to put into quarantine
     */
    public Quarantine(Node n) {
        super(Collections.<VM>emptySet(), Collections.singleton(n), true);
    }

    @Override
    public SatConstraintChecker<Quarantine> getChecker() {
        return new QuarantineChecker(this);
    }

    @Override
    public String toString() {
        return "quarantine(" + "node=" + getInvolvedNodes().iterator().next() + ", continuous" + ")";
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

}
