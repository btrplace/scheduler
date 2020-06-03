/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

  private final Node node;

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
