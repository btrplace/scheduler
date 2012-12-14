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

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * A constraint to specify the overbooking factor between
 * the physical resources offered by a node and the associated virtual resources
 * that are consumed by the VMs it hosts.
 * <p/>
 * To compute the virtual capacity of a server, its physical capacity is multiplied
 * by the overbooking factor. The result is then truncated.
 *
 * @author Fabien Hermenier
 */
public class Overbook extends SatConstraint {

    private String rcId;

    private double ratio;

    /**
     * Make a new constraint.
     *
     * @param nodes the nodes identifiers
     * @param rcId  the resource identifier
     * @param r     the overbooking ratio
     */
    public Overbook(Set<UUID> nodes, String rcId, double r) {
        super(Collections.<UUID>emptySet(), nodes);
        this.rcId = rcId;
        this.ratio = r;
        setContinuous(true);
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
        }
        return b.append(')').toString();
    }

    @Override
    public Sat isSatisfied(Model i) {
        Mapping cfg = i.getMapping();
        ShareableResource rc = i.getResource(rcId);
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
}


