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
import btrplace.model.view.ShareableResource;
import btrplace.plan.Action;
import btrplace.plan.DefaultReconfigurationPlanChecker;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.ReconfigurationPlanChecker;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * A constraint to specify and overbooking factor between
 * the physical resources offered by a node and the virtual resources
 * that are consumed by the VMs it hosts.
 * <p/>
 * To compute the virtual capacity of a server, its physical capacity is multiplied
 * by the overbooking factor. The result is then truncated.
 * <p/>
 * The restriction provided by the constraint can be either discrete or continuous.
 * If the restriction is continuous, then the constraint imposes the restriction
 * in the source model, during the reconfiguration and at the end.
 *
 * @author Fabien Hermenier
 */
public class Overbook extends SatConstraint {

    private String rcId;

    private double ratio;

    /**
     * Make a new constraint with a continuous restriction.
     *
     * @param nodes the nodes identifiers
     * @param rcId  the resource identifier
     * @param r     the overbooking ratio
     */
    public Overbook(Set<UUID> nodes, String rcId, double r) {
        this(nodes, rcId, r, true);
    }

    /**
     * Make a new constraint.
     *
     * @param nodes      the nodes identifiers
     * @param rcId       the resource identifier
     * @param r          the overbooking ratio
     * @param continuous {@code true} for a continuous restriction
     */
    public Overbook(Set<UUID> nodes, String rcId, double r, boolean continuous) {
        super(Collections.<UUID>emptySet(), nodes, continuous);
        this.rcId = rcId;
        this.ratio = r;
    }

    /**
     * Get the resource identifier.
     *
     * @return an identifier
     */
    public String getResource() {
        return this.rcId;
    }

    /**
     * Get the overbooking ratio.
     *
     * @return a positive integer
     */
    public double getRatio() {
        return this.ratio;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("overbook(nodes=");
        b.append(this.getInvolvedNodes()).append(", rc=").append(rcId).append(", ratio=").append(ratio);
        if (!isContinuous()) {
            b.append(", discrete");
        } else {
            b.append(", continuous");
        }
        return b.append(')').toString();
    }

    @Override
    public Sat isSatisfied(Model i) {
        Mapping cfg = i.getMapping();
        ShareableResource rc = (ShareableResource) i.getView(ShareableResource.VIEW_ID_BASE + rcId);
        if (rc == null) {
            return Sat.UNSATISFIED;
        }
        for (UUID nId : getInvolvedNodes()) {
            if (cfg.getOnlineNodes().contains(nId)) {
                //Server capacity with the ratio
                double capa = rc.get(nId) * ratio;
                //Minus the VMs usage
                for (UUID vmId : cfg.getRunningVMs(nId)) {
                    capa -= rc.get(vmId);
                    if (capa < 0) {
                        return Sat.UNSATISFIED;
                    }
                }
            }
        }
        return Sat.SATISFIED;
    }

    @Override
    public Sat isSatisfied(ReconfigurationPlan plan) {
        Sat res = isSatisfied(plan.getOrigin());
        if (!res.equals(Sat.SATISFIED)) {
            return Sat.UNSATISFIED;
        }
        Model cur = plan.getOrigin().clone();
        for (Action a : plan) {

            if (!a.apply(cur)) {
                return Sat.UNSATISFIED;
            }
            res = isSatisfied(cur);

            if (!res.equals(Sat.SATISFIED)) {
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

        Overbook that = (Overbook) o;

        return ratio == that.ratio &&
                getInvolvedNodes().equals(that.getInvolvedNodes()) &&
                rcId.equals(that.rcId);
    }

    @Override
    public int hashCode() {
        double result = rcId.hashCode();
        result = 31 * result + ratio;
        result = 31 * result + getInvolvedNodes().hashCode();
        return Double.valueOf(result).hashCode();
    }

    @Override
    public ReconfigurationPlanChecker getChecker() {
        return new Checker(this);
    }

    private class Checker extends DefaultReconfigurationPlanChecker {

        public Checker(Overbook o) {
            super(o);
        }

        @Override
        public boolean endsWith(Model i) {
            Mapping cfg = i.getMapping();
            ShareableResource rc = (ShareableResource) i.getView(ShareableResource.VIEW_ID_BASE + rcId);
            if (rc == null) {
                return false;
            }
            for (UUID nId : getInvolvedNodes()) {
                if (cfg.getOnlineNodes().contains(nId)) {
                    //Server capacity with the ratio
                    double capa = rc.get(nId) * ratio;
                    //Minus the VMs usage
                    for (UUID vmId : cfg.getRunningVMs(nId)) {
                        capa -= rc.get(vmId);
                        if (capa < 0) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }
}


