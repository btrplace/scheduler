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

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.model.ShareableResource;
import btrplace.plan.Action;
import btrplace.plan.ReconfigurationPlan;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Restrict the amount of virtual resources consumed by
 * the VMs hosted on each of the given nodes.
 * <p/>
 * The restriction provided by the constraint can be either discrete or continuous.
 * If it is discrete, the constraint only considers the model obtained as the end
 * of the reconfiguration process.
 * If the restriction is continuous, then the usage must never exceed
 * the given amount, in the source model, during the reconfiguration and at the end.
 * <p/>
 * By default, the constraint provides a discrete restriction.
 *
 * @author Fabien Hermenier
 */
public class SingleResourceCapacity extends SatConstraint {

    private String rcId;

    private int amount;

    /**
     * Make a new constraint.
     *
     * @param nodes  the involved servers.
     * @param rcId   the resource identifier
     * @param amount the maximum amount of resource to share among the hosted VMs
     */
    public SingleResourceCapacity(Set<UUID> nodes, String rcId, int amount) {
        super(Collections.<UUID>emptySet(), nodes, false);
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
    public Sat isSatisfied(Model i) {
        ShareableResource rc = i.getResource(rcId);
        if (rc == null) {
            return Sat.UNSATISFIED;
        }
        Mapping map = i.getMapping();
        for (UUID n : getInvolvedNodes()) {
            if (rc.sum(map.getRunningVMs(n), true) > amount) {
                return Sat.UNSATISFIED;
            }
        }
        return Sat.SATISFIED;

    }

    @Override
    public Sat isSatisfied(ReconfigurationPlan plan) {
        Model mo = plan.getOrigin();
        if (!isSatisfied(mo).equals(SatConstraint.Sat.SATISFIED)) {
            return Sat.UNSATISFIED;
        }
        mo = plan.getOrigin().clone();
        for (Action a : plan) {
            if (!a.apply(mo)) {
                return Sat.UNSATISFIED;
            }
            if (!isSatisfied(mo).equals(SatConstraint.Sat.SATISFIED)) {
                return Sat.UNSATISFIED;
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

        SingleResourceCapacity that = (SingleResourceCapacity) o;
        return getInvolvedNodes().equals(that.getInvolvedNodes())
                && amount == that.amount && rcId.equals(that.rcId)
                && isContinuous() == that.isContinuous();
    }

    @Override
    public int hashCode() {
        int res = amount;
        res = 31 * res + rcId.hashCode();
        res = 31 * res + getInvolvedNodes().hashCode() + (isContinuous() ? 1 : 0);
        return res;
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
}
