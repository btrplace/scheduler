package org.btrplace.model.view.net;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Specific implementation of {@link Routing}.
 * Allows to import static routes and import a network topology from an xml file.
 * If a specific path is not found from static routing rules, it automatically looks for physical connections.
 *
 * The constructor must strictly match the parent signature {@link Routing(NetworkView)} for generic instantiation,
 * see {@link NetworkView#initRouting(Class)}.
 * However, the class can still be instantiated manually and attached to an existing network view.
 *
 * @author Vincent Kherbache
 * @see NetworkView#initRouting(Class)
 */
public class StaticRouting extends Routing {

    private Map<NodesMap, List<Link>> routes;

    /**
     * Make a new static routing.
     * 
     * @param net   the associated network view
     */
    public StaticRouting(NetworkView net) {
        super(net);
        routes = new HashMap<>();
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
     * Recursive method to get the first physical path found from a switch to a destination node.
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

    /**
     * Import an XML file representing a full network topology including nodes, switches, links and routes
     * An example of XML file can be found in {api/test/resources/network/routing-test.xml}
     * 
     * @param mo    the associated model (needed to manage nodes)
     * @param xml   the xml file to parse
     * @return  the full list of nodes (already existing or newly created)
     */
    public List<Node> importXML(Model mo, File xml) {

        List<Node> nodes = new ArrayList<>();

        try {
            if (!xml.exists()) throw new FileNotFoundException("File '" + xml.getName() + "' not found");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xml);
            doc.getDocumentElement().normalize();

            org.w3c.dom.Node root = doc.getDocumentElement();
            NodeList nList;

            // Parse nodes
            nList = ((Element) root).getElementsByTagName("node");
            for (int i = 0; i < nList.getLength(); i++) {
                org.w3c.dom.Node node = nList.item(i);

                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element elt = (Element) node;

                    // Get the node if exists or create it and add it to the list
                    int id = Integer.valueOf(elt.getAttribute("id"));
                    int cpu = Integer.valueOf(elt.getAttribute("cpu"));
                    int ram = Integer.valueOf(elt.getAttribute("ram"));
                    Node nd = null;
                    for (Node n : mo.getMapping().getAllNodes()) {
                        if (n.id() == id) { nd = n; break; }
                    }
                    if (nd == null) nd = mo.newNode(id);
                    nodes.add(nd);
                }
            }

            // Parse switches
            nList = ((Element) root).getElementsByTagName("switch");
            for (int i = 0; i < nList.getLength(); i++) {
                org.w3c.dom.Node node = nList.item(i);

                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element elt = (Element) node;

                    // Create the new Switch
                    int id = Integer.valueOf(elt.getAttribute("id"));
                    int capacity = Integer.valueOf(elt.getAttribute("capacity"));
                    net.newSwitch(id, capacity);
                }
            }

            // Parse links
            nList = ((Element) root).getElementsByTagName("link");
            for (int i = 0; i < nList.getLength(); i++) {
                org.w3c.dom.Node node = nList.item(i);

                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element elt = (Element) node;

                    // Connect elements
                    int id = Integer.valueOf(elt.getAttribute("id"));
                    int bandwidth = Integer.valueOf(elt.getAttribute("bandwidth"));
                    String leftType = elt.getAttribute("left").split("_")[0];
                    int leftId = Integer.valueOf(elt.getAttribute("left").split("_")[1]);
                    String rightType = elt.getAttribute("right").split("_")[0];
                    int rightId = Integer.valueOf(elt.getAttribute("right").split("_")[1]);

                    if (leftType.equals("switch")) {
                        Switch leftSwitch = null;
                        for (Switch sw : net.getSwitches()) {
                            if (sw.id() == leftId) leftSwitch = sw;
                        }
                        if (leftSwitch == null)
                            throw new Exception("Cannot find the switch with id '" + leftId + "'");

                        if (rightType.equals("switch")) {
                            Switch rightSwitch = null;
                            for (Switch sw : net.getSwitches()) {
                                if (sw.id() == rightId) rightSwitch = sw;
                            }
                            if (rightSwitch == null)
                                throw new Exception("Cannot find the switch with id '" + rightId + "'");

                            // Connect two switches
                            net.connect(bandwidth, leftSwitch, rightSwitch);
                        } else {
                            Node rightNode = null;
                            for (Node n : nodes) {
                                if (n.id() == rightId) rightNode = n;
                            }
                            if (rightNode == null)
                                throw new Exception("Cannot find the node with id '" + rightId + "'");

                            // Connect switch to node
                            net.connect(id, bandwidth, leftSwitch, rightNode);
                        }
                    } else {
                        Node leftNode = null;
                        for (Node n : nodes) {
                            if (n.id() == leftId) leftNode = n;
                        }
                        if (leftNode == null) throw new Exception("Cannot find the node with id '" + leftId + "'");

                        if (rightType.equals("switch")) {
                            Switch rightSwitch = null;
                            for (Switch sw : net.getSwitches()) {
                                if (sw.id() == rightId) rightSwitch = sw;
                            }
                            if (rightSwitch == null)
                                throw new Exception("Cannot find the switch with id '" + rightId + "'");

                            // Connect node to switch
                            net.connect(id, bandwidth, rightSwitch, leftNode);
                        } else {
                            throw new Exception("Cannot link two nodes together !");
                        }
                    }
                }
            }

            // Parse routes
            nList = ((Element) root).getElementsByTagName("route");
            for (int i = 0; i < nList.getLength(); i++) {
                org.w3c.dom.Node node = nList.item(i);

                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element elt = (Element) node;

                    int src = Integer.valueOf(elt.getAttribute("src"));
                    Node srcNode = null;
                    for (Node n : nodes) {
                        if (n.id() == src) srcNode = n;
                    }
                    if (srcNode == null) throw new Exception("Cannot find the node with id '" + src + "'");

                    int dst = Integer.valueOf(elt.getAttribute("dst"));
                    Node dstNode = null;
                    for (Node n : nodes) {
                        if (n.id() == dst) dstNode = n;
                    }
                    if (dstNode == null) throw new Exception("Cannot find the node with id '" + dst + "'");

                    NodesMap nodesMap = new NodesMap(srcNode, dstNode);
                    List<Link> links = new ArrayList<>();

                    // Parse route's links
                    NodeList lnks = elt.getElementsByTagName("lnk");
                    for (int j = 0; j < lnks.getLength(); j++) {
                        Element lnk = (Element) lnks.item(j);
                        int id = Integer.valueOf(lnk.getAttribute("id"));
                        // Looking for the link and adding it to the list
                        for (Link l : net.getLinks()) {
                            if (l.id() == id) { links.add(l); break; }
                        }
                    }

                    // Add the new route
                    setStaticRoute(nodesMap, links);
                }
            }
        }
        catch (Exception e) {
            System.err.println("Error during XML import: " + e.toString());
            e.printStackTrace();
            return null;
        }

        return nodes;
    }

    @Override
    public List<Link> getPath(Node n1, Node n2) {

        NodesMap nodesMap = new NodesMap(n1, n2);

        // Check for a static route
        for (NodesMap nm : routes.keySet()) {
            if (nm.equals(nodesMap)) {
                return routes.get(nm);
            }
        }

        // If not found, return the first physical path found
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
        StaticRouting srouting = new StaticRouting(net);
        srouting.routes.putAll(routes);
        return srouting;
    }
}
