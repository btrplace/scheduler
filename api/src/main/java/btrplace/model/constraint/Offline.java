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
import btrplace.model.constraint.checker.OfflineChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;

import java.util.Collection;

/**
 * A constraint to force a set of nodes at being offline.
 * <p/>
 * The restriction provided by the constraint is discrete.
 * however, if some of the nodes are already offline, then
 * their state will be unchanged.
 *
 * @author Fabien Hermenier
 */
public class Offline extends NodeStateConstraint {

    /**
     * Make a new constraint.
     *
     * @param nodes the nodes to set offline
     */
    public Offline(Collection<Node> nodes) {
        super("offline", nodes);
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new OfflineChecker(this);
    }

}
