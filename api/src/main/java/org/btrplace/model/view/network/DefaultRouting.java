/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view.network;

import org.btrplace.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link Routing}.
 * Allows to retrieve physical path (L2) between network elements by looking at physical connections.
 * <p>
 * If instantiated manually, it should be first attached to an existing network view,
 * see {@link #setNetwork(Network)}.
 *
 * @author Vincent Kherbache
 * @see #setNetwork(Network)
 */
public class DefaultRouting extends Routing {

    /**
     * Recursive method to get the first physical path found from a switch to a destination node
     *
     * @param   currentPath the current or initial path containing the link(s) crossed
     * @param   sw the current switch to browse
     * @param   dst the destination node to reach
     * @return  the ordered list of links that make the path to dst
     */
    private Map<Link, Boolean> getFirstPhysicalPath(Map<Link, Boolean> currentPath, Switch sw, Node dst) {

        // Iterate through the current switch's links
        for (Link l : net.getConnectedLinks(sw)) {
            // Wrong link
            if (currentPath.containsKey(l)) {
                continue;
            }

            /*
             * Go through the link and track the direction for full-duplex purpose
             * From switch to element => false : DownLink
             * From element to switch => true : UpLink
             */
            currentPath.put(l, !l.getSwitch().equals(sw));

            // Check what is after
            if (l.getElement() instanceof Node) {
                // Node found, path complete
                if (l.getElement().equals(dst)) {
                    return currentPath;
                }
            }
            else {
                // Go to the next switch
                Map<Link, Boolean> recall = getFirstPhysicalPath(
                        currentPath, l.getSwitch().equals(sw) ? (Switch) l.getElement() : l.getSwitch(), dst);
                // Return the complete path if found
                if (!recall.isEmpty()) {
                    return recall;
                }
            }
            // Wrong link, go back
            currentPath.remove(l);
        }
        // No path found through this switch
        return new LinkedHashMap<>();
    }

    @Override
    public List<Link> getPath(Node n1, Node n2) {
        if (net == null || n1.equals(n2)) {
            return Collections.emptyList();
        }

        // Initialize the cache
        if (routingCache == null) {
            int cacheSize = net.getConnectedNodes().size();
            routingCache = new LinkedHashMap[cacheSize][cacheSize]; // OK for the warning
        }

        // Fill the cache if needed
        if (routingCache[n1.id()][n2.id()] == null) {
            // Create an initial path from the node to its switch => true : UpLink
            Map<Link, Boolean> initialPath = new LinkedHashMap<>();
            initialPath.put(net.getConnectedLinks(n1).get(0), true);
            // Get the first physical path found between the two nodes
            routingCache[n1.id()][n2.id()] =
                    getFirstPhysicalPath(
                            initialPath, // Only one link per node
                            net.getConnectedLinks(n1).get(0).getSwitch(), // A node is always connected to a switch
                            n2
                    );
        }

        return new ArrayList<>(routingCache[n1.id()][n2.id()].keySet());
    }

    @Override
    public LinkDirection getLinkDirection(Node n1, Node n2, Link l) {
        if (n1.equals(n2)) {
            return LinkDirection.NONE;
        }

        // Initialize the cache (should be already done)
        if (routingCache == null) {
            int cacheSize = net.getConnectedNodes().size();
            routingCache = new LinkedHashMap[cacheSize][cacheSize]; // OK for the warning
        }

        // Fill the appropriate cache entry from getPath method if needed
        if (routingCache[n1.id()][n2.id()] == null) {
            getPath(n1, n2);
        }

        // Link is not on route!
        if (!routingCache[n1.id()][n2.id()].containsKey(l)) {
            return LinkDirection.NONE;
        }

        // Return the direction if the given link in on path
        if (routingCache[n1.id()][n2.id()].containsKey(l)) {
            return routingCache[n1.id()][n2.id()].get(l) ?
                    LinkDirection.DOWNLINK : LinkDirection.UPLINK;
        }
        return LinkDirection.NONE;
    }

    @Override
    public Routing copy() {
        DefaultRouting clone = new DefaultRouting();
        clone.net = net; // Do not associate view->routing, only routing->view
        return clone;
    }
}
