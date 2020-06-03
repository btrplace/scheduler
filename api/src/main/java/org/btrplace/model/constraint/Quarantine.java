/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.constraint;

import org.btrplace.model.Node;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A constraint to put a node into quarantine.
 * Running VMs in the quarantine zone can not leave their node
 * while no VMs outside the quarantine zone can be hosted on
 * the node in quarantine.
 * <p>
 * The restriction provided by the constraint is only continuous.
 *
 * @author Fabien Hermenier
 */
@SideConstraint(args = {"n : nodes"}, inv = "(!(v : hosted(n)) Root(v)) & (!(v2 /: hosted(n)) Ban(v2, {n}))")
public class Quarantine implements SatConstraint {

  private final Node node;

    /**
     * Make a new constraint.
     *
     * @param n the node to put into quarantine
     */
    public Quarantine(Node n) {
        this.node = n;
    }

    @Override
    public SatConstraintChecker<Quarantine> getChecker() {
        return new QuarantineChecker(this);
    }

    @Override
    public String toString() {
        return "quarantine(" + "node=" + node + ", continuous" + ")";
    }

    @Override
    public boolean setContinuous(boolean b) {
        return b;
    }

    /**
     * Instantiate constraints for a collection of nodes.
     *
     * @param nodes the nodes to integrate
     * @return the associated list of constraints
     */
    public static List<Quarantine> newQuarantine(Collection<Node> nodes) {
        return nodes.stream().map(Quarantine::new).collect(Collectors.toList());
    }

    @Override
    public Collection<Node> getInvolvedNodes() {
        return Collections.singleton(node);
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Quarantine that = (Quarantine) o;
        return Objects.equals(node, that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }
}
