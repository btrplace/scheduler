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

    private int id;
    private int capacity;

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
