/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view.network;

import org.btrplace.model.Element;
import org.btrplace.model.PhysicalElement;

/**
 * Model a switch
 * A switch should not be instantiated directly. Use {@link Network#newSwitch()} instead.
 *
 * @author Vincent Kherbache
 * @see Network#newSwitch()
 */
public class Switch implements Element, PhysicalElement, NetworkElement {

  private final int id;
  private final int capacity;

  /**
   * The element identifier.
   */
  public static final String TYPE = "switch";

  /**
   * Make a new Switch.
   *
   * @param id the switch identifier.
     * @param c  the maximal capacity of the switch.
     */
    public Switch(int id, int c) {
        this.id = id;
        capacity = c;
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
    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Switch)) {
            return false;
        }

        Switch sw = (Switch) o;

        return id == sw.id();
    }

    @Override
    public int hashCode() {
        return id;
    }
}
