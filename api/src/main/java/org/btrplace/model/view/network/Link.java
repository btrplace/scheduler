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

package org.btrplace.model.view.network;

import org.btrplace.model.PhysicalElement;

import java.util.List;

/**
 * Model a link that connects a {@link Switch} to a {@link PhysicalElement} (links between two nodes are not supported).
 * 
 * A link should not be instantiated directly. Typically, new links are automatically created by connecting
 * PhysicalElement together, see {@link Network#connect(int, Switch, org.btrplace.model.PhysicalElement)}
 * 
 * @author Vincent Kherbache
 * @see Network#connect(int, Switch, org.btrplace.model.PhysicalElement)
 */
public class Link implements NetworkElement {

    private int id;
    private int capacity;
    private Switch sw;
    private PhysicalElement pe;

    /**
     * Make a new Link.
     * Should not be used directly.
     *
     * @param id        the link identifier, avoid duplicate
     * @param capacity  the link maximal capacity (or bandwidth)
     * @param sw        the switch to connect
     * @param pe        the physical element to connect
     *
     * @see Network#connect(int, Switch, List)
     */
    public Link(int id, int capacity, Switch sw, PhysicalElement pe) {
        this.id = id;
        this.capacity = capacity;
        this.sw = sw;
        this.pe = pe;
    }

    /**
     * Get the connected switch.
     * Note: A link can be connected between two switches, this method retrieve the main switch,
     * the second one can be retrieved by calling {@link #getElement()} and casted to a Switch.
     * 
     * @return the switch
     */
    public Switch getSwitch() { return sw; }

    /**
     * Get the connected physical element (switch or node)
     *
     * @return the physical element
     */
    public PhysicalElement getElement() { return pe; }

    @Override
    public int id() { return id; }

    @Override
    public int getCapacity() { return capacity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Link)) {
            return false;
        }

        Link l = (Link) o;

        // Only check the id (there should be no duplicate)
        return id == l.id();
    }
}
