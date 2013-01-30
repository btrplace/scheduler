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
import btrplace.model.ShareableResource;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Restrict the cumulated amount of virtual resources consumed by
 * the VMs hosted on the given nodes.
 * <p/>
 * The restriction provided by the constraint can be either discrete or continuous.
 * If it is discrete, the constraint only considers the model obtained as the end
 * of the reconfiguration process.
 * If the restriction is continuous, then the cumulated resource usage must never exceed
 * the given amount, in the source model, during the reconfiguration and at the end.
 * <p/>
 * By default, the restriction is discrete.
 *
 * @author Fabien Hermenier
 */
public class CumulatedResourceCapacity extends SatConstraint {

    private int qty;

    private String rcId;

    /**
     * Make a new constraint.
     *
     * @param servers the server involved in the constraint
     * @param rc      the resource to consider
     * @param amount  the total amount of resource consumed by all the VMs running on the given servers
     */
    public CumulatedResourceCapacity(Set<UUID> servers, String rc, int amount) {
        super(Collections.<UUID>emptySet(), servers, false);
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
    public Sat isSatisfied(Model i) {
        ShareableResource rc = (ShareableResource) i.getView(ShareableResource.VIEW_ID_BASE + rcId);
        if (rc == null) {
            return Sat.UNSATISFIED;
        }

        int remainder = qty;
        for (UUID id : getInvolvedNodes()) {
            if (i.getMapping().getOnlineNodes().contains(id)) {
                for (UUID vmId : i.getMapping().getRunningVMs(id)) {
                    remainder -= rc.get(vmId);
                    if (remainder < 0) {
                        return Sat.UNSATISFIED;
                    }
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

        CumulatedResourceCapacity that = (CumulatedResourceCapacity) o;

        return qty == that.qty &&
                rcId.equals(that.rcId) &&
                getInvolvedNodes().equals(that.getInvolvedNodes()) &&
                this.isContinuous() == that.isContinuous();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + qty;
        result = 31 * result + rcId.hashCode();
        result = 31 * result + getInvolvedNodes().hashCode() + (isContinuous() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("cumulatedResourceCapacity(")
                .append("nodes=").append(getInvolvedNodes())
                .append(", rc=").append(rcId)
                .append(", amount=").append(qty);

        if (isContinuous()) {
            b.append(", continuous");
        } else {
            b.append(", discrete");
        }
        b.append(')');

        return b.toString();
    }

}
