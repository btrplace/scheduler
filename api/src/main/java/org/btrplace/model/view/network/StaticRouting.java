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
 * Statical implementation of {@link Routing}.
 * Requires to specify routes manually, along with each link direction for full-duplex purpose,
 * see {@link #setStaticRoute(NodesMap, Map)}.
 *
 * If instantiated manually, it should be first attached to an existing network view, see {@link #setNetwork(Network)}.
 *
 * @author Vincent Kherbache
 * @see #setNetwork(Network)
 */
public class StaticRouting extends Routing {

    private Map<NodesMap, Map<Link, Boolean>> routes;

    /**
     * Make a new static routing.
     */
    public StaticRouting() {
        routes = new HashMap<>();
    }

    /**
     * Get the static route between two given nodes.
     * 
     * @param nm    the nodes map
     * @return the static route. {@code null} if not found
     */
    public List<Link> getStaticRoute(NodesMap nm) {

        Map<Link, Boolean> route = routes.get(nm);
        
        if (route == null) {
            return null;
        }
        
        return new ArrayList<>(route.keySet());
    }

    /**
     * Get all the registered static routes.
     *
     * @return  the static routes
     */
    public Map<NodesMap, Map<Link, Boolean>> getStaticRoutes() {
        return routes;
    }

    /**
     * Manually add a static route between two nodes.
     *
     * @param nm    a node mapping containing two nodes: the source and the destination node.
     * @param links an insert-ordered map of link<->direction representing the path between the two nodes.
     */
    public void setStaticRoute(NodesMap nm, Map<Link, Boolean> links) {
        routes.put(nm, links); // Only one route between two nodes (replace the old route)
    }

    @Override
    public List<Link> getPath(Node n1, Node n2) {

        // Check for a static route
        Map<Link, Boolean> route = routes.get(new NodesMap(n1, n2));
        if (route == null) {
            return null;
        }
        // Return the list of links
        return new ArrayList<>(route.keySet());
    }

    @Override
    public Boolean getLinkDirection(Node n1, Node n2, Link l) {

        // Check for a static route
        Map<Link, Boolean> route = routes.get(new NodesMap(n1, n2));
        if (route == null) {
            return null;
        }
        // Return the direction if the given link is on path
        return route.containsKey(l) ? route.get(l) : null;
    }

    @Override
    public Routing copy() {
        StaticRouting clone = new StaticRouting();
        clone.net = net; // Do not associate view->routing, only routing->view
        clone.routes.putAll(routes);
        return clone;
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
