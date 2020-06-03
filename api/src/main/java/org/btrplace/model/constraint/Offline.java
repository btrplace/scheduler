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
 * A constraint to force a node at being offline.
 * The constraint is discrete
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"n : nodes"}, inv = "nodeState(n) = offline")
public class Offline extends SimpleConstraint {

  private final Node node;
    /**
     * Make a new constraint.
     *
     * @param n          the node to set offline
     */
    public Offline(Node n) {
        super(false);
        node = n;
    }

    @Override
    public boolean setContinuous(boolean b) {
        return !b;
    }

    @Override
    public OfflineChecker getChecker() {
        return new OfflineChecker(this);
    }

    @Override
    public String toString() {
        return "offline(node=" + node + ")";
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
        return Objects.equals(node, offline.node);
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
    public static List<Offline> newOffline(Collection<Node> nodes) {
        return nodes.stream().map(Offline::new).collect(Collectors.toList());
    }

  /**
   * Instantiate discrete constraints for a collection of nodes.
   *
   * @param nodes the nodes to integrate
   * @return the associated list of constraints
   */
  public static List<Offline> newOffline(Node... nodes) {
    return newOffline(Arrays.asList(nodes));
  }
}
