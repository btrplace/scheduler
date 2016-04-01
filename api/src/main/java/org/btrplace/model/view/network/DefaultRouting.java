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

import java.util.*;

/**
 * Default implementation of {@link Routing}.
 * Allows to retrieve physical path (L2) between network elements by looking at physical connections.
 *
 * If instantiated manually, it should be first attached to an existing network view,
 * see {@link #setNetwork(Network)}.
 *
 * @author Vincent Kherbache
 * @see #setNetwork(Network)
 */
public class DefaultRouting extends Routing {

    /**
     * Make a new default routing
     */
    public DefaultRouting() {}

    /**
     * Recursive method to get the first physical path found from a switch to a destination node
     *
     * @param   currentPath the current or initial path containing the link(s) crossed
     * @param   sw the current switch to browse
     * @param   dst the destination node to reach
     * @return  the ordered list of links that make the path to dst
     */
    private LinkedHashMap<Link, Boolean> getFirstPhysicalPath(LinkedHashMap<Link, Boolean> currentPath, Switch sw, Node dst) {

        // Iterate through the current switch's links
        for (Link l : net.getConnectedLinks(sw)) {
            // Wrong link
            if (currentPath.keySet().contains(l)) continue;
            // Go through the link and track the direction for full-duplex purpose
            if (l.getSwitch().equals(sw)) {
                // From switch to element => false : DownLink
                currentPath.put(l, false);
            }
            else {
                // From element to switch => true : UpLink
                currentPath.put(l, true);
            }

            // Check what is after
            if (l.getElement() instanceof Node) {
                // Node found, path complete
                if (l.getElement().equals(dst)) return currentPath;
            }
            else {
                // Go to the next switch
                LinkedHashMap<Link, Boolean>  recall = getFirstPhysicalPath(
                        currentPath, l.getSwitch().equals(sw) ? (Switch) l.getElement() : l.getSwitch(), dst);
                // Return the complete path if found
                if (!recall.isEmpty()) return recall;
            }
            // Wrong link, go back
            //currentPath.remove(new ArrayList<>(currentPath.keySet()).get(currentPath.size()-1));//Use list to keep order
            currentPath.remove(l);
        }
        // No path found through this switch
        return new LinkedHashMap<>();
    }

    @Override
    public List<Link> getPath(Node n1, Node n2) {

        if (net == null) { return Collections.emptyList(); }

        // Initialize the cache
        if (routingCache == null) {
            int cacheSize = net.getConnectedNodes().size();
            routingCache = new LinkedHashMap[cacheSize][cacheSize]; // OK for the warning
        }

        // Fill the cache if needed
        if (routingCache[n1.id()][n2.id()] == null) {
            // Get the first physical path found between the two nodes
            LinkedHashMap<Link, Boolean> initialPath = new LinkedHashMap<Link, Boolean>();
            // From element to switch => true : UpLink
            initialPath.put(net.getConnectedLinks(n1).get(0), true);
            routingCache[n1.id()][n2.id()] =
                    getFirstPhysicalPath(
                            initialPath, // Only one link per node
                            net.getConnectedLinks(n1).get(0).getSwitch(), // A node is always connected to a switch
                            n2
                    );
        }

        return new ArrayList<Link>(routingCache[n1.id()][n2.id()].keySet());
    }

    @Override
    public Boolean getLinkDirection(Node n1, Node n2, Link l) {

        // Initialize the cache (should be already done)
        if (routingCache == null) {
            int cacheSize = net.getConnectedNodes().size();
            routingCache = new LinkedHashMap[cacheSize][cacheSize]; // OK for the warning
        }

        // Fill the needed cache entry from getPath method
        if (routingCache[n1.id()][n2.id()] == null) {
            getPath(n1, n2);
        }

        // Link is not on route!
        if (!routingCache[n1.id()][n2.id()].keySet().contains(l)) return null;

        // Return the direction
        return routingCache[n1.id()][n2.id()].get(l);
    }

    @Override
    public int getMaxBW(Node n1, Node n2) {
        int max = Integer.MAX_VALUE;
        for (Link inf : getPath(n1, n2)) {
            if (inf.getCapacity() < max) {
                max = inf.getCapacity();
            }
        }
        return max;
    }

    @Override
    public Routing clone() {
        DefaultRouting clone = new DefaultRouting();
        clone.net = net; // Do not associate view->routing, only routing->view
        return clone;
    }
}
