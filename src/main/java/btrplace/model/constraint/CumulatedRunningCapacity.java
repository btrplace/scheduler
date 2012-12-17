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
 * If the restriction is continuous, then the cumulated usage must never exceed
 * the given amount, in the source model, during the reconfiguration and at the end.
 * <p/>
 * By default, the restriction is discrete
 *
 * @author Fabien Hermenier
 */
public class CumulatedRunningCapacity extends SatConstraint {

    private int qty;

    /**
     * Make a new constraint.
     *
     * @param servers the server involved in the constraint
     * @param amount  the total amount of resource consumed by all the VMs running on the given servers
     */
    public CumulatedRunningCapacity(Set<UUID> servers, int amount) {
        super(Collections.<UUID>emptySet(), servers, false);
        this.qty = amount;
    }

    @Override
    public Sat isSatisfied(Model i) {
        int remainder = qty;
        for (UUID id : getInvolvedNodes()) {
            if (i.getMapping().getOnlineNodes().contains(id)) {
                remainder -= i.getMapping().getRunningVMs(id).size();
                if (remainder < 0) {
                    return Sat.UNSATISFIED;
                }
            }
        }
        return Sat.SATISFIED;
    }


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
}
