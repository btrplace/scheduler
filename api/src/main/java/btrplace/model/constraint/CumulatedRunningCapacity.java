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

import btrplace.model.SatConstraint;
import btrplace.model.constraint.checker.CumulatedRunningCapacityChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Restrict to a given value, the cumulated amount of VMs running
 * on the given set of nodes.
 * <p/>
 * The restriction provided by the constraint can be either discrete or continuous.
 * If it is discrete, the constraint only considers the model obtained as the end
 * of the reconfiguration process.
 * <p/>
 * If the restriction is continuous, then the cumulated usage must never exceed
 * the given amount, in the source model, during the reconfiguration and at the end.
 *
 * @author Fabien Hermenier
 */
public class CumulatedRunningCapacity extends SatConstraint {

    private int qty;

    /**
     * Make a new constraint having a discrete restriction.
     *
     * @param servers the server involved in the constraint
     * @param amount  the total amount of resource consumed by all the VMs running on the given servers
     */
    public CumulatedRunningCapacity(Set<UUID> servers, int amount) {
        this(servers, amount, false);
    }

    /**
     * Make a new constraint.
     *
     * @param servers    the server involved in the constraint
     * @param amount     the total amount of resource consumed by all the VMs running on the given servers
     * @param continuous {@code true} for a continuous restriction
     */
    public CumulatedRunningCapacity(Set<UUID> servers, int amount, boolean continuous) {
        super(Collections.<UUID>emptySet(), servers, continuous);
        this.qty = amount;
    }

    /*@Override
    public Sat isSatisfied(ReconfigurationPlan p) {
        Model mo = p.getOrigin();
        if (!isSatisfied(mo).equals(Sat.SATISFIED)) {
            return Sat.UNSATISFIED;
        }
        mo = p.getOrigin().clone();
        for (Action a : p) {
            if (!a.apply(mo)) {
                return Sat.UNSATISFIED;
            }
            if (!isSatisfied(mo).equals(Sat.SATISFIED)) {
                return Sat.UNSATISFIED;
            }
        }
        return Sat.SATISFIED;
    }*/

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        CumulatedRunningCapacity that = (CumulatedRunningCapacity) o;

        return qty == that.qty &&
                getInvolvedNodes().equals(that.getInvolvedNodes());
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
    public int hashCode() {
        return 31 * qty + getInvolvedNodes().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("cumulatedRunningCapacity(")
                .append("nodes=").append(getInvolvedNodes())
                .append(", amount=").append(qty);
        if (!isContinuous()) {
            b.append(", discrete");
        } else {
            b.append(", continuous");
        }
        b.append(')');

        return b.toString();
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new CumulatedRunningCapacityChecker(this);
    }

}
