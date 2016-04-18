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
 * A constraint to force a node at being offline.
 *
 * @author Fabien Hermenier
 */
public class Offline implements SatConstraint {

    private Node node;

    private boolean continuous;

    /**
     * Make a new discrete constraint.
     *
     * @param n the node to set offline
     */
    public Offline(Node n) {
        this(n, false);
    }

    /**
     * Make a new constraint.
     *
     * @param n          the node to set offline
     * @param continuous {@code true} for a continuous restriction
     */
    public Offline(Node n, boolean continuous) {
        node = n;
        this.continuous = continuous;
    }


    @Override
    public OfflineChecker getChecker() {
        return new OfflineChecker(this);
    }

    @Override
    public String toString() {
        return "offline(node=" + node + ", " + (continuous ? "continuous" : "discrete") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Offline offline = (Offline) o;
        return continuous == offline.continuous &&
                Objects.equals(node, offline.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, continuous);
    }

    @Override
    public Collection<Node> getInvolvedNodes() {
        return Collections.singleton(node);
    }

    @Override
    public Collection<VM> getInvolvedVMs() {
        return Collections.emptyList();
    }

    @Override
    public boolean isContinuous() {
        return continuous;
    }

    @Override
    public boolean setContinuous(boolean b) {
        continuous = b;
        return true;
    }

    /**
     * Instantiate discrete constraints for a collection of nodes.
     *
     * @param nodes the nodes to integrate
     * @return the associated list of constraints
     */
    public static List<Offline> newOffline(Collection<Node> nodes) {
        List<Offline> l = new ArrayList<>(nodes.size());
        for (Node n : nodes) {
            l.add(new Offline(n));
        }
        return l;
    }
}
