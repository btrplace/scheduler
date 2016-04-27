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

import org.btrplace.model.Node;

import java.util.*;

/**
 * Specific implementation of {@link Routing}.
 * If a specific path is not found from static routing rules, it automatically looks for physical connections.
 * <p>
 * If instantiated manually, it should be first attached to an existing network view, see {@link #setNetwork(Network)}.
 *
 * @author Vincent Kherbache
 * @see #setNetwork(Network)
 */
public class StaticRouting extends Routing {

    private Map<NodesMap, List<Link>> routes;

    /**
     * Make a new static routing.
     */
    public StaticRouting() {
        routes = new HashMap<>();
    }

    /**
     * Get the static route between two given nodes.
     *
     * @param nm the nodes map
     * @return the static route
     */
    public List<Link> getStaticRoute(NodesMap nm) {
        return routes.get(nm);
    }

    /**
     * Get all the registered static routes.
     *
     * @return the static routes
     */
    public Map<NodesMap, List<Link>> getStaticRoutes() {
        return routes;
    }

    /**
     * Manually add a static route between two nodes.
     * TODO: add routes between 2 PhysicalElements and check for an optimal path between nodes using this global mapping
     *
     * @param nm    a node mapping containing two nodes: the source and the destination node.
     * @param links an ordered list of links representing the path between the two nodes.
     */
    public void setStaticRoute(NodesMap nm, List<Link> links) {
        routes.put(nm, links); // Only one route between two nodes (replace the old route)
    }

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
                // From switch to element => false : UpLink
                currentPath.put(l, false);
            }
            else {
                // From element to switch => true : DownLink
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
            List<Link> tmpListToRemove = new ArrayList<>(currentPath.keySet()); // Use a list to keep insert order
            currentPath.remove(tmpListToRemove.get(tmpListToRemove.size()-1));
        }
        // No path found through this switch
        return new LinkedHashMap<>();
    }

    @Override
    public List<Link> getPath(Node n1, Node n2) {

        NodesMap nodesMap = new NodesMap(n1, n2);

        // Check for a static route
        List<Link> l = routes.get(nodesMap);
        if (l != null) {
            return l;
        }

        // If not found, return the first physical path found

        if (net == null) { return Collections.emptyList(); }

        // Initialize the cache
        if (routingCache == null) {
            int cacheSize = net.getConnectedNodes().size();
            routingCache = new LinkedHashMap[cacheSize][cacheSize]; // OK for the warning
        }

        // Fill the cache if needed
        if (routingCache[n1.id()][n2.id()] == null) {
            // Get the first physical path found between the two nodes
            LinkedHashMap<Link, Boolean> initialPath = new LinkedHashMap<>();
            // From element to switch => true : DownLink
            initialPath.put(net.getConnectedLinks(n1).get(0), true);
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
    public Boolean getLinkDirection(Node n1, Node n2, Link l) {

        // Initialize the cache (should be already known)
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
    public Routing copy() {
        StaticRouting srouting = new StaticRouting();
        srouting.net = net; // Do not associate view->routing, only routing->view
        srouting.routes.putAll(routes);
        return srouting;
    }

    /**
     * Inner class that map two nodes to ease the routing.
     * It allows to easily compare and differentiate and the nodes pair (src, dst).
     */
    public static class NodesMap {
        private Node n1;
        private Node n2;

        public NodesMap(Node n1, Node n2) {
            this.n1 = n1;
            this.n2 = n2;
        }

        public Node getSrc() {
            return n1;
        }

        public Node getDst() {
            return n2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof NodesMap)) {
                return false;
            }
            return ((NodesMap) o).getSrc().equals(n1) && ((NodesMap) o).getDst().equals(n2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(n1, n2);
        }
    }
}
