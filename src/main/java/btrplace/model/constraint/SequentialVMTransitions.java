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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * For the action associated to a list of VMs
 * to be executed in a given order.
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
    public Sat isSatisfied(Model i) {
        return Sat.SATISFIED;
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
                .append(")").toString();
    }
}
