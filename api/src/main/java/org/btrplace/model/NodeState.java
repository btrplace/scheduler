/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model;

/**
 * The possible state for a node.
 */
public enum NodeState {
    ONLINE(1), OFFLINE(2);

  private final int v;

    /**
     * Unique identifier for the state.
     *
     * @param val the state value
     */
    NodeState(int val) {
        this.v = val;
    }

    /**
     * Get the enum identifier.
     *
     * @return the identifier
     */
    public int value() {
        return v;
    }
}
