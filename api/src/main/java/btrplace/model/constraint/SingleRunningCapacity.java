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
import btrplace.model.constraint.checker.SatConstraintChecker;
import btrplace.model.constraint.checker.SingleRunningCapacityChecker;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Restrict the hosting capacity of each of the given node to a given
 * amount of VMs.
 * <p/>
 * The restriction provided by the constraint can be either discrete or continuous.
 * If it is discrete, the constraint only considers the model obtained as the end
 * of the reconfiguration process.
 * <p/>
 * If the restriction is continuous, then the usage must never exceed
 * the given amount, in the source model, during the reconfiguration and at the end.
 *
 * @author Fabien Hermenier
 */
public class SingleRunningCapacity extends SatConstraint {

    private int amount;

    /**
     * Make a new constraint having a discrete restriction.
     *
     * @param nodes the involved servers.
     * @param qty   the maximum amount of resource to share among the hosted VMs
     */
    public SingleRunningCapacity(Set<Node> nodes, int qty) {
        this(nodes, qty, false);
    }

    /**
     * Make a new constraint.
     *
     * @param nodes      the involved servers.
     * @param qty        the maximum amount of resource to share among the hosted VMs
     * @param continuous {@code true} for a continuous restriction
     */
    public SingleRunningCapacity(Set<Node> nodes, int qty, boolean continuous) {
        super(Collections.<VM>emptySet(), nodes, continuous);
        this.amount = qty;
    }


    /**
     * Get the amount of resources
     *
     * @return a positive integer
     */
    public int getAmount() {
        return this.amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SingleRunningCapacity that = (SingleRunningCapacity) o;
        return getInvolvedNodes().equals(that.getInvolvedNodes())
                && amount == that.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInvolvedNodes(), amount, isContinuous());
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("singleRunningCapacity(")
                .append("nodes=").append(getInvolvedNodes())
                .append(", amount=").append(amount);
        if (!isContinuous()) {
            b.append(", discrete");
        } else {
            b.append(", continuous");
        }
        return b.append(")").toString();
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new SingleRunningCapacityChecker(this);
    }


}
