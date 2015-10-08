package org.btrplace.model.view.net;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.ModelView;
import org.btrplace.model.view.NamingService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A network view that allows to create switches and connect them to nodes or together using links.
 * It contains the mapping between connected physical elements and allows to retrieve a full path
 * between two nodes through a specific routing implementation {@link Routing#getPath(Node, Node)} 
 * 
 * @author Vincent Kherbache
 * @see Routing#getPath(Node, Node)
 */
public class NetworkView implements ModelView, Cloneable {

    private List<Switch> switches;
    private List<Link> links;
    private Map<PhysicalElement, List<Link>> mapping;
    private Routing routing;
    private String viewId;
    private SwitchBuilder swBuilder;
    private LinkBuilder lnBuilder;

    /**
     * The global view identifier, assuming that each model must contain a unique NetworkView
     */
    public static final String VIEW_ID = "NetworkView";

    /**
     * Make a new default instance that rely on a {@link DefaultRouting}, a {@link DefaultSwitchBuilder} and a
     * {@link DefaultLinkBuilder}.
     */
    public NetworkView() {
        this(DefaultRouting.class, new DefaultSwitchBuilder(), new DefaultLinkBuilder());
    }

    /**
     * Make a new custom instance relying on a given switch builder, a {@link DefaultRouting} and a
     * {@link DefaultLinkBuilder}.
     *
     * @param sb the switch builder to use
     */
    public NetworkView(SwitchBuilder sb) {
        this(DefaultRouting.class, sb, new DefaultLinkBuilder());
    }

    /**
     * Make a new custom instance relying on a given link builder, a {@link DefaultRouting} and a
     * {@link DefaultSwitchBuilder}.
     *
     * @param lb the link builder to use
     */
    public NetworkView(LinkBuilder lb) {
        this(DefaultRouting.class, new DefaultSwitchBuilder(), lb);
    }

    /**
     * Make a new custom instance relying on a given routing implementation, a {@link DefaultSwitchBuilder} and a
     * {@link DefaultLinkBuilder}.
     *
     * @param routingClass the routing class to use
     */
    public NetworkView(Class<? extends Routing> routingClass) {
        this(routingClass, new DefaultSwitchBuilder(), new DefaultLinkBuilder());
    }

    /**
     * Make a new custom instance relying on a given routing implementation, a given switch builder, and a
     * {@link DefaultLinkBuilder}.
     *
     * @param routingClass the routing class to use
     * @param sb           the switch builder to use
     */
    public NetworkView(Class<? extends Routing> routingClass, SwitchBuilder sb) {
        this(routingClass, sb, new DefaultLinkBuilder());
    }

    /**
     * Make a new custom instance relying on a given routing implementation, a given link builder, and a
     * {@link DefaultSwitchBuilder}.
     *
     * @param routingClass the routing class to use
     * @param lb           the link builder to use
     */
    public NetworkView(Class<? extends Routing> routingClass, LinkBuilder lb) {
        this(routingClass, new DefaultSwitchBuilder(), lb);
    }

    /**
     * Make a new full custom instance relying on a given routing implementation, a given switch builder, and a
     * given link builder
     *
     * @param routingClass the routing class to use
     * @param sb           the switch builder to use
     * @param lb           the link builder to use
     */
    public NetworkView(Class<? extends Routing> routingClass, SwitchBuilder sb, LinkBuilder lb) {
        this.viewId = VIEW_ID;
        switches = new ArrayList<>();
        links = new ArrayList<>();
        mapping = new HashMap<>();
        swBuilder = sb;
        lnBuilder = lb;
        initRouting(routingClass);
    }

    /**
     * Create a new switch with a specific identifier and a given maximal capacity
     *
     * @param id       the switch identifier
     * @param capacity the switch maximal capacity (put a nul or negative number for non-blocking switch)
     * @return  the switch
     */
    public Switch newSwitch(int id, int capacity) {
        Switch s = swBuilder.newSwitch(id, capacity);
        switches.add(s);
        return s;
    }

    /**
     * Create a new switch wih a given maximal capacity
     *
     * @param capacity the switch maximal capacity (put a nul or negative number for non-blocking switch)
     * @return the switch
     */
    public Switch newSwitch(int capacity) {
        Switch s = swBuilder.newSwitch(capacity);
        switches.add(s);
        return s;
    }

    /**
     * Create a new non-blocking switch
     *
     * @return the switch
     */
    public Switch newSwitch() {
        return newSwitch(-1);
    }

    /**
     * Create a new link first with a specific identifier and a given maximal capacity (or bandwidth),
     * then connect the link between the given switch and physical element.
     *
     * @param id        the new link identifier
     * @param bandwidth the maximal bandwidth for the connection
     * @param sw        the switch to connect
     * @param pe        the physical element to connect
     * @return the generated link that connects both elements
     */
    public Link connect(int id, int bandwidth, Switch sw, PhysicalElement pe) {
        // Create a new link with a specific id
        Link link = lnBuilder.newLink(id, bandwidth, sw, pe);
        links.add(link);

        // Map the link with both elements
        mapLink(link, sw, pe);
        return link;
    }

    /**
     * First create a new link with a given maximal bandwidth and connect it between the
     * switch and the physical element.
     *
     * @param bandwidth the maximal bandwidth for the connection
     * @param sw        the switch to connect
     * @param pe        the physical element to connect
     * @return the generated link that connects both elements
     */
    public Link connect(int bandwidth, Switch sw, PhysicalElement pe) {
        // Create a new link
        Link link = lnBuilder.newLink(bandwidth, sw, pe);
        links.add(link);

        // Map the link with both elements
        mapLink(link, sw, pe);
        return link;
    }

    /**
     * Create connections between a single switch and multiple physical elements
     *
     * @param bandwidth the maximal bandwidth for the connection
     * @param sw        the switch to connect
     * @param pelts     a list of physical elements to connect
     * @return a list of links
     */
    public List<Link> connect(int bandwidth, Switch sw, List<? extends PhysicalElement> pelts) {
        return pelts.stream().map(pe -> connect(bandwidth, sw, pe)).collect(Collectors.toList());
    }

    /**
     * Create connections between a single switch and multiple physical elements
     *
     * @param bandwidth the maximal bandwidth for the connection
     * @param sw        the switch to connect
     * @param pelts     a list of physical elements to connect
     * @return a list of links
     */
    public List<Link> connect(int bandwidth, Switch sw, PhysicalElement... pelts) {
        List<Link> links = new ArrayList<>();
        for (PhysicalElement pe : pelts) {
            links.add(connect(bandwidth, sw, pe));
        }
        return links;
    }

    /**
     * Create connections between a single switch and multiple nodes
     *
     * @param bandwidth the maximal bandwidth for the connection
     * @param sw        the switch to connect
     * @param nodes     a list of nodes to connect
     * @return a list of links
     */
    public List<Link> connect(int bandwidth, Switch sw, Node... nodes) {
        List<Link> links = new ArrayList<>();
        for (Node n : nodes) {
            links.add(connect(bandwidth, sw, n));
        }
        return links;
    }

    /**
     * Get the full list of links
     * 
     * @return  the list of links
     */
    public List<Link> getLinks() {
        return links;
    }

    /**
     * Get the full list of switches
     * 
     * @return  the list of switches
     */
    public List<Switch> getSwitches() { return switches; }

    /**
     * Get the routing implementation
     *
     * @return the routing used
     */
    public Routing getRouting() { return routing; }

    /**
     * Get the mapping of each physical elements to their links
     *
     * @return  the mapping
     */
    public Map<PhysicalElement, List<Link>> getMapping() { return mapping; }

    /**
     * Get the list of links connected to a given physical element
     * 
     * @param pe    the physical element
     * @return  the list of links
     */
    public List<Link> getConnectedLinks(PhysicalElement pe) {
        return mapping.get(pe);
    }

    /**
     * Get the full list of nodes that have been connected into the network
     * 
     * @return  the list of nodes
     */
    public List<Node> getConnectedNodes() {
        List<Node> nodes = new ArrayList<>();
        for (PhysicalElement pe : mapping.keySet()) {
            if (pe instanceof Node) { nodes.add((Node) pe); }
        }
        return nodes;
    }

    /**
     * Generate a dot file (diagram) of the current network infrastructure, included all connected elements and links.
     *
     * @param mo                the model to use, it may contains naming services for switches or nodes that will
     *                          replace the generic names mainly based on the id number.
     * @param out               the output dot file to create
     * @param fromLeftToRight   if true: force diagram's shapes to be placed side by side (create larger diagrams) 
     * @return  true if it succeed, false otherwise
     */
    public boolean generateDot(Model mo, String out, boolean fromLeftToRight) {

        int numLink;
        List<Node> nodes = getConnectedNodes();
        Set<Link> drawedLinks = new HashSet<>();

        // Try to retrieve naming services from the model, if provided
        NamingService<Node> nsNodes = null;
        NamingService<Switch> nsSwitches = null;
        if (mo != null) {
            nsNodes = (NamingService<Node>) mo.getView(NamingService.ID + "node");
            nsSwitches = (NamingService<Switch>) mo.getView(NamingService.ID + "switch");
        }

        try {
            BufferedWriter dot = new BufferedWriter(new FileWriter(out));
            dot.append("digraph G {\n");
            if (fromLeftToRight) { dot.append("rankdir=LR;\n"); }
            // Draw nodes
            for (Node n : nodes) {
                dot.append("node").
                        append(String.valueOf(n.id())).
                        append(" [shape=box, color=green, label=\"").
                        append((nsNodes==null)?"Node "+String.valueOf(n.id()):nsNodes.resolve(n)).
                        append("\"];\n");
            }
            // Draw switches
            for (Switch s : switches) {
                dot.append("switch").
                        append(String.valueOf(s.id())).
                        append(" [shape=circle, color=blue, label=\"").
                        append((nsSwitches==null)?"Switch "+String.valueOf(s.id()):nsSwitches.resolve(s)).
                        append((s.getCapacity()>0)?"\\n["+bitsToString(s.getCapacity())+"/s"+"]":"");
                dot.append("\"];\n");
            }
            // Draw links
            for (Switch s : switches) {
                for (Link l : getConnectedLinks(s)) {
                    if (!drawedLinks.contains(l)) {
                        dot.append("switch").append(String.valueOf(s.id())).
                                append(" -> ");
                        if (l.getElement() instanceof Node) {
                            dot.append("node").append(String.valueOf(((Node) l.getElement()).id()));
                        }
                        else {
                            Switch dsw = l.getSwitch().equals(s) ? (Switch) l.getElement() : l.getSwitch();
                            dot.append("switch").append(String.valueOf(dsw.id()));
                        }
                        dot.append(" [arrowhead=none, color=red, label=\"").
                                append(bitsToString(l.getCapacity())).append("/s").
                                append("\"]\n");
                        drawedLinks.add(l);
                    }
                }
            }
            dot.append("}\n");
            dot.flush();
            dot.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Generate a dot file (diagram) of the current network infrastructure, included all connected elements and links.
     *
     * @param out               the output dot file to create
     * @param fromLeftToRight   if true: force diagram's shapes to be placed side by side (create larger diagrams) 
     * @return  true if it succeed, false otherwise
     */
    public boolean generateDot(String out, boolean fromLeftToRight) {
        return generateDot(null, out, fromLeftToRight);
    }

    /**
     * Generate a dot file (diagram) of the current network infrastructure, included all connected elements and links.
     *
     * @param out   the output dot file to create
     * @return  true if it succeed, false otherwise
     */
    public boolean generateDot(String out) {
        return generateDot(null, out, false);
    }

    /**
     * Set the routing implementation class to use
     *
     * @param routingClass the routing class
     */
    private void initRouting(Class<? extends Routing> routingClass) {
        try {
            routing = routingClass.getDeclaredConstructor(NetworkView.class).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the routing implementation to use
     *
     * @param routing the routing implementation
     */
    public void setRouting(Routing routing) {
        this.routing = routing;
        routing.setNetwork(this);
    }

    /**
     * Keep a mapping between a given switch to a given physical element through the given link.
     *
     * @param link  the link connecting the switch and the physical element
     * @param sw    the switch to map
     * @param pe    the physical element to map
     */
    private void mapLink(Link link, Switch sw, PhysicalElement pe) {
        // Map switch to the link
        if (!mapping.containsKey(sw)) { mapping.put(sw, new ArrayList<>()); }
        mapping.get(sw).add(link);

        // Map physical element to the link
        if (!mapping.containsKey(pe)) { mapping.put(pe, new ArrayList<>()); }
        mapping.get(pe).add(link);
    }

    /**
     * Convert megabits into a more 'human readable' format (from 'long' to 'String')
     *
     * @param megabits  the amount in megabit (mb)
     * @return  a String containing a pretty formatted output
     */
    private String bitsToString(long megabits) {
        int unit = 1000;
        if (megabits < unit) return megabits + " mb";
        int exp = (int) (Math.log(megabits) / Math.log(unit));
        return new DecimalFormat("#.##").format(megabits / Math.pow(unit, exp)) + "GTPE".charAt(exp-1) + "b";
    }

    @Override
    public String getIdentifier() {
        return viewId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return this.viewId.equals(((NetworkView) o).getIdentifier());
    }

    @Override
    public ModelView clone() {
        NetworkView net = new NetworkView();
        net.routing = routing.clone();
        net.routing.setNetwork(net);
        net.switches.addAll(switches);
        net.links.addAll(links);
        net.mapping.putAll(mapping);
        net.lnBuilder = lnBuilder.clone();
        net.swBuilder = swBuilder.clone();
        return net;
    }

    @Override
    public boolean substituteVM(VM curId, VM nextId) {
        return false;
    }
}
