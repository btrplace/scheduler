/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

/**
 * Model a node.
 * A node should not be instantiated directly. Use {@link Model#newNode()} instead.
 *
 * @author Fabien Hermenier
 * @see Model#newNode()
 */
public class Node implements Element,PhysicalElement {

  private final int id;

    /**
     * The element identifier
     */
    public static final String TYPE = "node";

    /**
     * Make a new node.
     *
     * @param i the node identifier.
     */
    public Node(int i) {
        this.id = i;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String toString() {
        return TYPE + "#" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Node)) {
            return false;
        }

        Node node = (Node) o;

        return id == node.id();
    }

    @Override
    public int hashCode() {
        return id;
    }
}
