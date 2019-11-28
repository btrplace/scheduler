/*
 * Copyright (c) 2019 University Nice Sophia Antipolis
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A constraint to force a node at being online.
 * The constraint is only discrete.
 *
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"n : nodes"}, inv = "$nodeState(n) = online")
public class Online extends SimpleConstraint {

    private Node node;

    /**
     * Make a new constraint.
     *
     * @param n          the node to set online
     */
    public Online(Node n) {
        super(false);
        node = n;
    }

    @Override
    public OnlineChecker getChecker() {
        return new OnlineChecker(this);
    }

    @Override
    public String toString() {
        return "online(node=" + node + ")";
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Online online = (Online) o;
        return Objects.equals(node, online.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }

    @Override
    public Collection<Node> getInvolvedNodes() {
        return Collections.singleton(node);
    }

    /**
     * Instantiate discrete constraints for a collection of nodes.
     *
     * @param nodes the nodes to integrate
     * @return the associated list of constraints
     */
    public static List<Online> newOnline(Collection<Node> nodes) {
        return nodes.stream().map(Online::new).collect(Collectors.toList());
    }

    /**
     * Instantiate discrete constraints for a collection of nodes.
     *
     * @param nodes the nodes to integrate
     * @return the associated list of constraints
     */
    public static List<Online> newOnline(Node... nodes) {
        return newOnline(Arrays.asList(nodes));
    }
}
