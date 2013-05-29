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
import btrplace.model.constraint.checker.QuarantineChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;

import java.util.Collections;
import java.util.Set;

/**
 * A constraint to put some nodes into quarantine.
 * running VMs in the quarantine zone can not leave their node
 * while no VMs outside the quarantine zone can be hosted on
 * the nodes in quarantine.
 * <p/>
 * The restriction provided by the constraint is only continuous.
 *
 * @author Fabien Hermenier
 */
public class Quarantine extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param nodes the set of nodes to put into quarantine
     */
    public Quarantine(Set<Node> nodes) {
        super(Collections.<VM>emptySet(), nodes, true);
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new QuarantineChecker(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Quarantine that = (Quarantine) o;
        return getInvolvedNodes().equals(that.getInvolvedNodes());
    }

    @Override
    public int hashCode() {
        return getInvolvedNodes().hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder("quarantine(")
                .append("nodes=").append(getInvolvedNodes())
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

}
