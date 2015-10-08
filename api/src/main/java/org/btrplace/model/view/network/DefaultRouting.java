package org.btrplace.model.view.net;

import org.btrplace.model.Node;

import java.util.*;

/**
 * Default implementation of {@link Routing}.
 * Allows to retrieve physical path (L2) between network elements by looking at physical connections.
 *
 * The constructor must strictly match the parent signature {@link Routing(NetworkView)} for generic instantiation,
 * see {@link NetworkView#initRouting(Class)}.
 * However, the class can still be instantiated manually and attached to an existing network view.
 *
 * @author Vincent Kherbache
 * @see NetworkView#initRouting(Class)
 */
public class DefaultRouting extends Routing {

    /**
     * Make a new default routing
     *
     * @param net   the associated network view
     */
    public DefaultRouting(NetworkView net) {
        super(net);
    }
    
    /**
     * Recursive method to get the first physical path found from a switch to a destination node
     *
     * @param   currentPath the current or initial path containing the link(s) crossed
     * @param   sw the current switch to browse
     * @param   dst the destination node to reach
     * @return  the ordered list of links that make the path to dst
     */
    private List<Link> getFirstPhysicalPath(List<Link> currentPath, Switch sw, Node dst) {

        // Iterate through the current switch's links
        for (Link l : net.getConnectedLinks(sw)) {
            // Wrong link
            if (currentPath.contains(l)) continue;
            // Go through the link
            currentPath.add(l);
            // Check what is after
            if (l.getElement() instanceof Node) {
                // Node found, path complete
                if (l.getElement().equals(dst)) return currentPath;
            }
            else {
                // Go to the next switch
                List<Link> recall = getFirstPhysicalPath(
                        currentPath, l.getSwitch().equals(sw) ? (Switch) l.getElement() : l.getSwitch(), dst);
                // Return the complete path if found
                if (!recall.isEmpty()) return recall;
            }
            // Wrong link, go back
            currentPath.remove(currentPath.size()-1);
        }
        // No path found through this switch
        return Collections.emptyList();
    }

    @Override
    public List<Link> getPath(Node n1, Node n2) {

        if (net == null) { return Collections.emptyList(); }

        // Get the first physical path found between the two nodes
        return getFirstPhysicalPath(
                new ArrayList<>(Arrays.asList(net.getConnectedLinks(n1).get(0))), // Only one link per node
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
    public Routing clone() {
        return new DefaultRouting(net);
    }
}
