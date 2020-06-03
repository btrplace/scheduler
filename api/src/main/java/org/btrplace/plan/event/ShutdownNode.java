/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;

import java.util.Objects;

/**
 * An action to shutdown an online node.
 * The node will be in the offline state once the action applied.
 *
 * @author Fabien Hermenier
 */
public class ShutdownNode extends Action implements NodeEvent {

  private final Node node;

    /**
     * Create a new shutdown action on an online node.
     *
     * @param n     The node to stop
     * @param start the moment the action starts
     * @param end   the moment the action is finished
     */
    public ShutdownNode(Node n, int start, int end) {
        super(start, end);
        this.node = n;
    }

    @Override
    public Node getNode() {
        return node;
    }


    /**
     * Test the equality with another object.
     *
     * @param o The object to compare with
     * @return true if o is an instance of Shutdown and if both actions act on the same node
     */
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        ShutdownNode that = (ShutdownNode) o;
        return this.node.equals(that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStart(), getEnd(), node);
    }

    @Override
    public String pretty() {
        return "shutdown(" + "node=" + node + ")";
    }

    /**
     * Put the node offline on a model
     *
     * @param c the model
     * @return {@code true} if the node was online and is set offline. {@code false} otherwise
     */
    @Override
    public boolean applyAction(Model c) {
        Mapping map = c.getMapping();
        return !map.isOffline(node) && map.addOfflineNode(node);
    }

    @Override
    public Object visit(ActionVisitor v) {
        return v.visit(this);
    }
}