/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Node;
import org.btrplace.model.view.ResourceRelated;

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
@SideConstraint(args = {"ns <: nodes", "id : string", "qty : int"}, inv = "sum({capa(i, id). i : ns}) <= qty")
public class ResourceCapacity extends SimpleConstraint implements ResourceRelated {

  private final Set<Node> nodes;

    private int qty;

    private String rcId;

    /**
     * Make a new discrete constraint on a single node.
     *
     * @param n      the n involved in the constraint
     * @param rc     the resource identifier
     * @param amount the maximum amount of resource consumed by all the VMs running on the given nodes. &gt;== 0
     */
    public ResourceCapacity(Node n, String rc, int amount) {
        this(Collections.singleton(n), rc, amount, false);
    }

    /**
     * Make a new constraint on a single node.
     *
     * @param n          the n involved in the constraint
     * @param rc         the resource identifier
     * @param amount     the maximum amount of resource consumed by all the VMs running on the given nodes. &gt;== 0
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
     * @param amount the maximum amount of resource consumed by all the VMs running on the given nodes. &gt;== 0
     */
    public ResourceCapacity(Set<Node> nodes, String rc, int amount) {
        this(nodes, rc, amount, false);
    }

    /**
     * Make a new constraint.
     *
     * @param nodes      the nodes involved in the constraint
     * @param rc         the resource identifier
     * @param amount     the maximum amount of resource consumed by all the VMs running on the given nodes. &gt;== 0
     * @param continuous {@code true} for a continuous restriction.
     */
    public ResourceCapacity(Set<Node> nodes, String rc, int amount, boolean continuous) {
        super(continuous);
        this.nodes = nodes;
        this.qty = amount;
        this.rcId = rc;

        if (amount < 0) {
            throw new IllegalArgumentException("The amount of resource must be >= 0");
        }
        this.qty = amount;
        this.rcId = rc;
    }

    @Override
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ResourceCapacity that = (ResourceCapacity) o;
        return qty == that.qty &&
                isContinuous() == that.isContinuous() &&
                Objects.equals(nodes, that.nodes) &&
                Objects.equals(rcId, that.rcId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, qty, rcId, isContinuous());
    }

    @Override
    public Set<Node> getInvolvedNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "resourceCapacity(" + "nodes=" + nodes
                + ", rc=" + rcId + ", amount=" + qty + ", " + (isContinuous() ? "continuous" : "discrete") + ')';
    }

    @Override
    public SatConstraintChecker<ResourceCapacity> getChecker() {
        return new ResourceCapacityChecker(this);
    }

}
