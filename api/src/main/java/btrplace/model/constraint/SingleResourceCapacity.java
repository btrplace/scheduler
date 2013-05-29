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
import btrplace.model.constraint.checker.SingleResourceCapacityChecker;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Restrict the amount of virtual resources consumed by
 * the VMs hosted on each of the given nodes.
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
public class SingleResourceCapacity extends SatConstraint {

    private String rcId;

    private int amount;

    /**
     * Make a new constraint with a discrete restriction.
     *
     * @param nodes  the involved servers.
     * @param rcId   the resource identifier
     * @param amount the maximum amount of resource to share among the hosted VMs
     */
    public SingleResourceCapacity(Set<Node> nodes, String rcId, int amount) {
        this(nodes, rcId, amount, false);
    }

    /**
     * Make a new constraint.
     *
     * @param nodes      the involved servers.
     * @param rcId       the resource identifier
     * @param amount     the maximum amount of resource to share among the hosted VMs
     * @param continuous {@code true} for a continuous restriction
     */
    public SingleResourceCapacity(Set<Node> nodes, String rcId, int amount, boolean continuous) {
        super(Collections.<VM>emptySet(), nodes, continuous);
        this.rcId = rcId;
        this.amount = amount;
    }

    /**
     * Get the amount of resources
     *
     * @return a positive integer
     */
    public int getAmount() {
        return this.amount;
    }

    /**
     * Get the resource identifier.
     *
     * @return a String
     */
    public String getResource() {
        return this.rcId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SingleResourceCapacity that = (SingleResourceCapacity) o;
        return getInvolvedNodes().equals(that.getInvolvedNodes())
                && amount == that.amount && rcId.equals(that.rcId)
                && isContinuous() == that.isContinuous();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInvolvedNodes(), rcId, amount, isContinuous());
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("singleResourceCapacity(")
                .append("nodes=").append(getInvolvedNodes())
                .append(", rc=").append(rcId)
                .append(", amount=").append(amount);

        if (isContinuous()) {
            b.append(", continuous");
        } else {
            b.append(", discrete");
        }

        return b.append(')').toString();
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new SingleResourceCapacityChecker(this);
    }

}
