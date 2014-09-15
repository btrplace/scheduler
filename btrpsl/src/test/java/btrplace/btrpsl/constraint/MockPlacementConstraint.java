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

package btrplace.btrpsl.constraint;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.model.constraint.SatConstraint;

import java.util.Collections;
import java.util.Set;

/**
 * A mock assignment constraint for test purpose.
 *
 * @author Fabien Hermenier
 */
public class MockPlacementConstraint extends SatConstraint {

    private Set<Set<VM>> sets;

    public MockPlacementConstraint(Set<Set<VM>> vmsets) {
        super(Collections.<VM>emptySet(), null, false);
        this.sets = vmsets;
    }

    public String toString() {
        return "mock(" + sets + ")";
    }

    @Override
    public boolean isSatisfied(Model model) {
        throw new UnsupportedOperationException();
    }
}
