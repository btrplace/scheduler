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

import org.btrplace.Copyable;
import org.btrplace.model.Node;

import java.util.Collections;
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
public abstract class Routing implements Copyable<Routing> {

    protected Network net;
    
    /**
     * Set the network view (recursively).
     */
    public void setNetwork(Network net) {
        this.net = net;
        if (net.getRouting() != this) {
            net.setRouting(this);
        }
    }
    
    /**
     * Get the path between two nodes.
     *
     * @param n1    the source node
     * @param n2    the destination node
     * @return the path consisting of an ordered list of links
     */
    public abstract List<Link> getPath(Node n1, Node n2);

    /**
     * Get the maximal bandwidth available between two nodes.
     *
     * @param n1    the source node
     * @param n2    the destination node
     * @return  the bandwidth
     */
    public abstract int getMaxBW(Node n1, Node n2);

    /**
     * Recursive method to get the first physical path found from a switch to a destination node.
     *
     * @param currentPath the current or initial path containing the link(s) crossed
     * @param sw          the current switch to browse
     * @param dst         the destination node to reach
     * @return the ordered list of links that make the path to dst
     */
    protected List<Link> getFirstPhysicalPath(List<Link> currentPath, Switch sw, Node dst) {

        // Iterate through the current switch's links
        for (Link l : net.getConnectedLinks(sw)) {
            // Wrong link
            if (currentPath.contains(l)) {
                continue;
            }
            // Go through the link
            currentPath.add(l);
            // Check what is after
            if (l.getElement() instanceof Node) {
                // Node found, path complete
                if (l.getElement().equals(dst)) {
                    return currentPath;
                }
            } else {
                // Go to the next switch
                List<Link> recall = getFirstPhysicalPath(
                        currentPath, l.getSwitch().equals(sw) ? (Switch) l.getElement() : l.getSwitch(), dst);
                // Return the complete path if found
                if (!recall.isEmpty()) {
                    return recall;
                }
            }
            // Wrong link, go back
            currentPath.remove(currentPath.size() - 1);
        }
        // No path found through this switch
        return Collections.emptyList();
    }

}
