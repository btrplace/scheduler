/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

import org.btrplace.model.VM;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A constraint to force the actions that change the given VMs state
 * to be executed in the given order.
 * <p>
 * The restriction provided by the constraint is only continuous.
 *
 * @author Fabien Hermenier
 */
public class Seq implements SatConstraint {

    private List<VM> order;

    /**
     * Make a new constraint.
     *
     * @param seq the order to ensure
     */
    public Seq(List<VM> seq) {
        Set<VM> s = new HashSet<>(seq);
        if (s.size() != seq.size()) {
            throw new IllegalArgumentException("The list of VMs must not contain duplicates");
        }
        order = seq;
    }

    @Override
    public List<VM> getInvolvedVMs() {
        return order;
    }


    @Override
    public String toString() {
        return "seq(vms=" + order + ", continuous" + ')';
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    @Override
    public SatConstraintChecker<Seq> getChecker() {
        return new SeqChecker(this);
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Seq seq = (Seq) o;
        return Objects.equals(order, seq.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order);
    }
}
