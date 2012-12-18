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

import btrplace.model.Model;
import btrplace.model.SatConstraint;

import java.util.Set;
import java.util.UUID;

/**
 * A constraint to force a set of VMs to be hosted on a single group of nodes
 * among those available.
 * <p/>
 * The restriction provided by the constraint is only discrete.
 * TODO: it is possible to get a continuous restriction. Looks fun ?
 *
 * @author Fabien Hermenier
 */
public class Among extends SatConstraint {

    public Among(Set<UUID> vms, Set<Set<UUID>> pGrps) {
        super(vms, null, false);
    }

    @Override
    public Sat isSatisfied(Model i) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
