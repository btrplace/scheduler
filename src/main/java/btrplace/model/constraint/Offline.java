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
 * A constraint to force a set of nodes at being offline.
 *
 * @author Fabien Hermenier
 */
public class Offline extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param nodes the nodes to set offline
     */
    public Offline(Set<UUID> nodes) {
        super(Collections.<UUID>emptySet(), nodes, false);
    }

    @Override
    public Sat isSatisfied(Model i) {
        Mapping c = i.getMapping();
        for (UUID n : getInvolvedNodes()) {
            if (!c.getOfflineNodes().contains(n)) {
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

        Offline that = (Offline) o;
        return getInvolvedNodes().equals(that.getInvolvedNodes());
    }

    @Override
    public int hashCode() {
        return getInvolvedNodes().hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder("offline(nodes=")
                .append(getInvolvedNodes())
                .append(")").toString();
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }

}
