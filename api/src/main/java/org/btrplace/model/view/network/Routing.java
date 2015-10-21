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

import org.btrplace.model.Node;

import java.util.List;

/**
 * A routing must be associated to a network view, it provides methods to get the path between two nodes and the
 * corresponding maximal bandwidth available on the path.
 * It can be either physical L2 or logical L3 routing depending on the desired implementation.
 * 
 * Note: A routing should be first associated to a network view, see {@link #setNetwork(Network)}.
 * 
 * @author Vincent Kherbache
 * @see #setNetwork(Network)
 */
public abstract class Routing {

    protected Network net;
    
    /**
     * Set the network view (recursively).
     */
    public void setNetwork(Network net) {
        this.net = net;
        if (net.getRouting() != this) net.setRouting(this);
    }
    
    /**
     * Get the path between two nodes.
     *
     * @param n1    the source node
     * @param n2    the destination node
     * @return the path consisting of an ordered list of links
     */
    abstract public List<Link> getPath(Node n1, Node n2);

    /**
     * Get the maximal bandwidth available between two nodes.
     *
     * @param n1    the source node
     * @param n2    the destination node
     * @return  the bandwidth
     */
    abstract public int getMaxBW(Node n1, Node n2);

    /**
     * Clone the routing.
     *
     * @return  a clone of the routing.
     */
    public abstract Routing clone();

}
