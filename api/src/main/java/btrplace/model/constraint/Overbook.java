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

import btrplace.model.constraint.checker.OverbookChecker;
import btrplace.model.constraint.checker.SatConstraintChecker;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

;

/**
 * A constraint to specify and overbooking factor between
 * the physical resources offered by a node and the virtual resources
 * that are consumed by the VMs it hosts.
 * <p/>
 * To compute the virtual capacity of a server, its physical capacity is multiplied
 * by the overbooking factor. The result is then truncated.
 * <p/>
 * The restriction provided by the constraint can be either discrete or continuous.
 * If the restriction is discrete, then the constraint imposes the restriction
 * only on the end of the reconfiguration process (the resulting model).
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
    public Overbook(Set<Integer> nodes, String rcId, double r) {
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
    public Overbook(Set<Integer> nodes, String rcId, double r, boolean continuous) {
        super(Collections.<Integer>emptySet(), nodes, continuous);
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
        return Objects.hash(getInvolvedNodes(), rcId, ratio, isContinuous());
    }

    @Override
    public SatConstraintChecker getChecker() {
        return new OverbookChecker(this);
    }

}


