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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @Override
    public List<Link> getPath(Node n1, Node n2) {

        if (net == null) {
            return Collections.emptyList();
        }

        // Get the first physical path found between the two nodes
        return getFirstPhysicalPath(
                new ArrayList<>(Collections.singletonList(net.getConnectedLinks(n1).get(0))), // Only one link per node
                net.getConnectedLinks(n1).get(0).getSwitch(), // A node is always connected to a switch
                n2
        );
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
    public Routing copy() {
        DefaultRouting clone = new DefaultRouting();
        clone.net = net; // Do not associate view->routing, only routing->view
        return clone;
    }
}
