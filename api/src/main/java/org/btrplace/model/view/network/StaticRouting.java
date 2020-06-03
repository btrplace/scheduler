/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view.network;

import org.btrplace.model.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

  private final Map<NodesMap, Map<Link, Boolean>> routes;

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
     * @return the static routes
     */
    public Map<NodesMap, Map<Link, Boolean>> getStaticRoutes() {
        return routes;
    }

    /**
     * Manually add a static route between two nodes.
     *
     * @param nm    a node mapping containing two nodes: the source and the destination node.
     * @param links an insert-ordered map of (link,direction) representing the path between the two nodes.
     */
    public void setStaticRoute(NodesMap nm, Map<Link, Boolean> links) {
        routes.put(nm, links); // Only one route between two nodes (replace the old route)
    }

    @Override
    public List<Link> getPath(Node n1, Node n2) {

        // Check for a static route
        Map<Link, Boolean> route = routes.get(new NodesMap(n1, n2));
        if (route == null) {
            return Collections.emptyList();
        }
        // Return the list of links
        return new ArrayList<>(route.keySet());
    }

    @Override
    public LinkDirection getLinkDirection(Node n1, Node n2, Link l) {

        // Check for a static route
        Map<Link, Boolean> route = routes.get(new NodesMap(n1, n2));
        if (route == null) {
            return LinkDirection.NONE;
        }

        // Return the direction if the given link is on path
        if (route.containsKey(l)) {
            return route.get(l) ? LinkDirection.DOWNLINK : LinkDirection.UPLINK;
        }
        return LinkDirection.NONE;
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
    private final Node n1;
    private final Node n2;

    /**
     * Make a new pair.
     *
     * @param n1 the first node
     * @param n2 the second node
     */
    public NodesMap(Node n1, Node n2) {
      this.n1 = n1;
      this.n2 = n2;
        }

        /**
         * Get the first node of the pair.
         *
         * @return a node
         */
        public Node getSrc() {
            return n1;
        }

        /**
         * Get the second node of the pair.
         * @return a node
         */
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
