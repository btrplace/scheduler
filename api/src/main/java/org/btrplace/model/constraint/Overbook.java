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

import java.util.*;

/**
 * A constraint to specify and overbooking factor between
 * the physical resources offered by a node and the virtual resources
 * that are consumed by the VMs it hosts.
 * <p>
 * To compute the virtual capacity of a server, its physical capacity is multiplied
 * by the overbooking factor. The result is then truncated.
 * <p>
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
     * @param n  the node
     * @param rc the resource identifier
     * @param r  the overbooking ratio, >= 1
     */
    public Overbook(Node n, String rc, double r) {
        this(n, rc, r, true);
    }

    /**
     * Make a new constraint.
     *
     * @param n          the nodes identifiers
     * @param rc         the resource identifier
     * @param r          the overbooking ratio, >= 1
     * @param continuous {@code true} for a continuous restriction
     */
    public Overbook(Node n, String rc, double r, boolean continuous) {
        super(Collections.<VM>emptySet(), Collections.singleton(n), continuous);
        if (r < 1.0d) {
            throw new IllegalArgumentException("The overbooking ratio must be >= 1.0");
        }
        this.rcId = rc;
        this.ratio = r;
    }

    /**
     * Instantiate constraints for a collection of nodes.
     *
     * @param nodes the nodes to integrate
     * @param rc    the resource identifier
     * @param r     the overbooking ratio, >= 1
     * @return the associated list of continuous constraints
     */
    public static List<Overbook> newOverbooks(Collection<Node> nodes, String rc, double r) {
        List<Overbook> l = new ArrayList<>(nodes.size());
        for (Node n : nodes) {
            l.add(new Overbook(n, rc, r));
        }
        return l;
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
     * @return a ratio >= 1
     */
    public double getRatio() {
        return this.ratio;
    }

    @Override
    public String toString() {
        return "overbook(node=" + this.getInvolvedNodes().iterator().next()
                + ", rc=" + rcId + ", ratio=" + ratio + ", " + restrictionToString() + ')';
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && ratio == ((Overbook) o).ratio && rcId.equals(((Overbook) o).rcId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rcId, ratio);
    }

    @Override
    public SatConstraintChecker<Overbook> getChecker() {
        return new OverbookChecker(this);
    }

}


