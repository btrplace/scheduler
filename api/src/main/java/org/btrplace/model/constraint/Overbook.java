/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
import org.btrplace.model.view.ResourceRelated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
@SideConstraint(args = {"n : nodes", "id : string", "qty : float"}, inv = "sum({cons(v, id). v : running(n)}) * qty < capa(n, id)")
public class Overbook extends SimpleConstraint implements ResourceRelated {

    private String rcId;

    private double ratio;

    private Node node;

    /**
     * Make a new constraint with a continuous restriction.
     *
     * @param n  the node
     * @param rc the resource identifier
     * @param r  the overbooking ratio, &gt;= 1
     */
    public Overbook(Node n, String rc, double r) {
        this(n, rc, r, true);
    }

    /**
     * Make a new constraint.
     *
     * @param n          the nodes identifiers
     * @param rc         the resource identifier
     * @param r          the overbooking ratio, &gt;= 1
     * @param continuous {@code true} for a continuous restriction
     */
    public Overbook(Node n, String rc, double r, boolean continuous) {
        super(continuous);
        this.node = n;
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
     * @param r     the overbooking ratio, &gt;= 1
     * @return the associated list of continuous constraints
     */
    public static List<Overbook> newOverbooks(Collection<Node> nodes, String rc, double r) {
        return nodes.stream().map(n -> new Overbook(n, rc, r)).collect(Collectors.toList());
    }

    @Override
    public String getResource() {
        return this.rcId;
    }

    /**
     * Get the overbooking ratio.
     *
     * @return a ratio &gt;= 1
     */
    public double getRatio() {
        return this.ratio;
    }

    @Override
    public String toString() {
        return "overbook(node=" + node
                + ", rc=" + rcId + ", ratio=" + ratio + ", " + (isContinuous() ? "continuous" : "discrete") + ')';
    }

    @Override
    public Collection<Node> getInvolvedNodes() {
        return Collections.singleton(node);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Overbook overbook = (Overbook) o;
        return Double.compare(overbook.ratio, ratio) == 0 &&
                isContinuous() == overbook.isContinuous() &&
                Objects.equals(rcId, overbook.rcId) &&
                Objects.equals(node, overbook.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rcId, ratio, node, isContinuous());
    }

    @Override
    public SatConstraintChecker<Overbook> getChecker() {
        return new OverbookChecker(this);
    }

}


