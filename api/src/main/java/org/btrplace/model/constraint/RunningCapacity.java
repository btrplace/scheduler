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

import org.btrplace.model.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Restrict to a given value, the total amount of VMs running
 * on the given set of nodes.
 * <p>
 * The restriction provided by the constraint can be either discrete or continuous.
 * If it is discrete, the constraint only considers the model obtained as the end
 * of the reconfiguration process.
 * <p>
 * If the restriction is continuous, then the total usage must never exceed
 * the given amount, in the source model, during the reconfiguration and at the end.
 *
 * @author Fabien Hermenier
 */
public class RunningCapacity extends SimpleConstraint {

    private int qty;

    private Set<Node> nodes;

    /**
     * Make a new discrete constraint on a single node
     *
     * @param n      the node involved in the constraint
     * @param amount the maximum amount running VMs running on the given node. >= 0
     */
    public RunningCapacity(Node n, int amount) {
        this(Collections.singleton(n), amount, false);
    }

    /**
     * Make a new constraint on a single node
     *
     * @param n          the node involved in the constraint
     * @param amount     the maximum amount running VMs running on the given node. >= 0
     * @param continuous {@code true} for a continuous restriction
     */
    public RunningCapacity(Node n, int amount, boolean continuous) {
        this(Collections.singleton(n), amount, continuous);
    }

    /**
     * Make a new constraint having a discrete restriction.
     *
     * @param nodes  the nodes involved in the constraint
     * @param amount the maximum amount running VMs running on the given nodes. >= 0
     */
    public RunningCapacity(Set<Node> nodes, int amount) {
        this(nodes, amount, false);
    }

    /**
     * Make a new constraint.
     *
     * @param nodes      the nodes involved in the constraint
     * @param amount     the maximum amount running VMs running on the given nodes. >= 0
     * @param continuous {@code true} for a continuous restriction
     */
    public RunningCapacity(Set<Node> nodes, int amount, boolean continuous) {
        super(continuous);
        this.nodes = nodes;
        this.qty = amount;
        if (amount < 0) {
            throw new IllegalArgumentException("The amount of VMs must be >= 0");
        }
    }

    /**
     * Get the amount of resources
     *
     * @return a positive integer
     */
    public int getAmount() {
        return this.qty;
    }

    @Override
    public String toString() {
        return "runningCapacity(" + "nodes=" + nodes
                + ", amount=" + qty + ", " + (isContinuous() ? "continuous" : "discrete") + ')';
    }

    @Override
    public Collection<Node> getInvolvedNodes() {
        return nodes;
    }

    @Override
    public SatConstraintChecker<RunningCapacity> getChecker() {
        return new RunningCapacityChecker(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RunningCapacity that = (RunningCapacity) o;
        return qty == that.qty &&
                isContinuous() == that.isContinuous() &&
                Objects.equals(nodes, that.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qty, nodes, isContinuous());
    }
}
