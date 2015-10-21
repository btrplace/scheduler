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

package org.btrplace.model.constraint.migration;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.SatConstraintChecker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A constraint to ensure no overlapping between a set of migrations.
 *
 * @author Vincent Kherbache
 */
public class Serialize extends SatConstraint {

    /**
     * Make a new constraint.
     *
     * @param vms the involved VMs
     */
    public Serialize(Collection<VM> vms) {
        super(vms, Collections.<Node>emptyList(), true);
    }

    /**
     * Make a new constraint.
     *
     * @param vms the involved VMs
     */
    public Serialize(VM... vms) {
        super(Arrays.asList(vms), Collections.<Node>emptyList(), true);
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new SerializeChecker(this);
    }

    @Override
    public String toString() {
        return "serialize(" + "vms=" + getInvolvedVMs() + ", " + restrictionToString() + ")";
    }
}
