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

import btrplace.model.constraint.checker.SatConstraintChecker;
import btrplace.model.constraint.checker.SequentialVMTransitionsChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A constraint to force the actions that change the given VMs state
 * to be executed in the given order.
 * <p/>
 * The restriction provided by the constraint is only continuous.
 *
 * @author Fabien Hermenier
 */
public class SequentialVMTransitions extends SatConstraint {

    private List<UUID> order;

    /**
     * Make a new constraint.
     *
     * @param seq the order to ensure
     */
    public SequentialVMTransitions(List<UUID> seq) {
        super(seq, new ArrayList<UUID>(), true);
        order = seq;
    }

    @Override
    public List<UUID> getInvolvedVMs() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SequentialVMTransitions that = (SequentialVMTransitions) o;
        return getInvolvedVMs().equals(that.getInvolvedVMs());
    }

    @Override
    public int hashCode() {
        return getInvolvedVMs().hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder("sequentialVMTransitions(")
                .append("vms=").append(getInvolvedVMs())
                .append(", continuous")
                .append(')').toString();
    }

    @Override
    public boolean setContinuous(boolean b) {
        if (b) {
            super.setContinuous(b);
        }
        return b;
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new SequentialVMTransitionsChecker(this);
    }

}
