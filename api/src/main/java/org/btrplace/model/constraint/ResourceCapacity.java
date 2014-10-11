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

package org.btrplace.model.constraint;

import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Restrict the total amount of virtual resources consumed by
 * the VMs hosted on the given nodes.
 * <p>
 * The restriction provided by the constraint can be either discrete or continuous.
 * If it is discrete, the constraint only considers the model obtained as the end
 * of the reconfiguration process.
 * <p>
 * If the restriction is continuous, then the total resource usage must never exceed
 * the given amount, in the source model, during the reconfiguration and at the end.
 *
 * @author Fabien Hermenier
 */
public class ResourceCapacity extends SatConstraint {

    private int qty;

    private String rcId;

    /**
     * Make a new discrete constraint on a single node.
     *
     * @param n      the n involved in the constraint
     * @param rc     the resource identifier
     * @param amount the maximum amount of resource consumed by all the VMs running on the given nodes. >= 0
     */
    public ResourceCapacity(Node n, String rc, int amount) {
        this(Collections.singleton(n), rc, amount, false);
    }

    /**
     * Make a new constraint on a single node.
     *
     * @param n          the n involved in the constraint
     * @param rc         the resource identifier
     * @param amount     the maximum amount of resource consumed by all the VMs running on the given nodes. >= 0
     * @param continuous {@code true} for a continuous restriction.
     */
    public ResourceCapacity(Node n, String rc, int amount, boolean continuous) {
        this(Collections.singleton(n), rc, amount, continuous);
    }

    /**
     * Make a new constraint with a discrete restriction.
     *
     * @param nodes  the nodes involved in the constraint
     * @param rc     the resource identifier
     * @param amount the maximum amount of resource consumed by all the VMs running on the given nodes. >= 0
     */
    public ResourceCapacity(Set<Node> nodes, String rc, int amount) {
        this(nodes, rc, amount, false);
    }

    /**
     * Make a new constraint.
     *
     * @param nodes      the nodes involved in the constraint
     * @param rc         the resource identifier
     * @param amount     the maximum amount of resource consumed by all the VMs running on the given nodes. >= 0
     * @param continuous {@code true} for a continuous restriction.
     */
    public ResourceCapacity(Set<Node> nodes, String rc, int amount, boolean continuous) {
        super(Collections.<VM>emptySet(), nodes, continuous);
        if (amount < 0) {
            throw new IllegalArgumentException("The amount of resource must be >= 0");
        }
        this.qty = amount;
        this.rcId = rc;
    }

    /**
     * Get the resource identifier.
     *
     * @return a String
     */
    public String getResource() {
        return this.rcId;
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
    public boolean equals(Object o) {
        return super.equals(o) && rcId.equals(((ResourceCapacity) o).rcId) && qty == ((ResourceCapacity) o).qty;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), qty, rcId);
    }

    @Override
    public String toString() {
        return "resourceCapacity(" + "nodes=" + getInvolvedNodes()
                + ", rc=" + rcId + ", amount=" + qty + ", " + restrictionToString() + ')';
    }

    @Override
    public SatConstraintChecker<ResourceCapacity> getChecker() {
        return new ResourceCapacityChecker(this);
    }

}
