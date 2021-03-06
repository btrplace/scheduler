/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.model.view.network;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.PhysicalElement;
import org.btrplace.model.VM;
import org.btrplace.model.view.ModelView;
import org.btrplace.model.view.NamingService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A network view that allows to create switches and connect them to nodes or together using links.
 * It contains the mapping between connected physical elements and allows to retrieve a full path
 * between two nodes through a specific routing implementation {@link Routing#getPath(Node, Node)}
 *
 * @author Vincent Kherbache
 * @see Routing#getPath(Node, Node)
 */
public class Network implements ModelView {

    private final List<Switch> switches;
    private final List<Link> links;
    private Routing routing;
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
    public Network() {
        this(new DefaultRouting(), new DefaultSwitchBuilder(), new DefaultLinkBuilder());
    }

    /**
     * Make a new custom instance relying on a given switch builder, a {@link DefaultRouting} and a
     * {@link DefaultLinkBuilder}.
     *
     * @param sb    the switch builder to use
     */
    public Network(SwitchBuilder sb) {
        this(new DefaultRouting(), sb, new DefaultLinkBuilder());
    }

    /**
     * Make a new custom instance relying on a given link builder, a {@link DefaultRouting} and a
     * {@link DefaultSwitchBuilder}.
     *
     * @param lb    the link builder to use
     */
    public Network(LinkBuilder lb) {
        this(new DefaultRouting(), new DefaultSwitchBuilder(), lb);
    }

    /**
     * Make a new custom instance relying on a given routing implementation, a {@link DefaultSwitchBuilder} and a
     * {@link DefaultLinkBuilder}.
     *
     * @param routing   the routing implementation to use
     */
    public Network(Routing routing) {
        this(routing, new DefaultSwitchBuilder(), new DefaultLinkBuilder());
    }

    /**
     * Make a new custom instance relying on a given routing implementation, a given switch builder, and a
     * {@link DefaultLinkBuilder}.
     *
     * @param routing   the routing implementation to use
     * @param sb        the switch builder to use
     */
    public Network(Routing routing, SwitchBuilder sb) {
        this(routing, sb, new DefaultLinkBuilder());
    }

    /**
     * Make a new custom instance relying on a given routing implementation, a given link builder, and a
     * {@link DefaultSwitchBuilder}.
     *
     * @param routing   the routing implementation to use
     * @param lb        the link builder to use
     */
    public Network(Routing routing, LinkBuilder lb) {
        this(routing, new DefaultSwitchBuilder(), lb);
    }

    /**
     * Make a new full custom instance relying on a given routing implementation, a given switch builder, and a
     * given link builder
     *
     * @param routing   the routing implementation to use
     * @param sb        the switch builder to use
     * @param lb        the link builder to use
     */
    public Network(Routing routing, SwitchBuilder sb, LinkBuilder lb) {
        switches = new ArrayList<>();
        links = new ArrayList<>();
        swBuilder = sb;
        lnBuilder = lb;
        setRouting(routing);
    }

    /**
     * Create a new switch with a specific identifier and a given maximal capacity
     *
     * @param id       the switch identifier
     * @param capacity the switch maximal capacity (put a nul or negative number for non-blocking switch)
     * @return the switch
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
        return newSwitch(Integer.MAX_VALUE);
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
        List<Link> l = new ArrayList<>();
        for (PhysicalElement pe : pelts) {
            l.add(connect(bandwidth, sw, pe));
        }
        return l;
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
        List<Link> l = new ArrayList<>();
        for (Node n : nodes) {
            l.add(connect(bandwidth, sw, n));
        }
        return l;
    }

    /**
     * Get the full list of links
     *
     * @return the list of links
     */
    public List<Link> getLinks() {
        return links;
    }

    /**
     * Get the full list of switches
     *
     * @return the list of switches
     */
    public List<Switch> getSwitches() {
        return switches;
    }

    /**
     * Get the routing implementation
     *
     * @return the routing used
     */
    public Routing getRouting() {
        return routing;
    }

    /**
     * Get the list of links connected to a given physical element
     *
     * @param pe    the physical element
     * @return the list of links
     */
    public List<Link> getConnectedLinks(PhysicalElement pe) {
        List<Link> myLinks = new ArrayList<>();
        for (Link l : this.links) {
            if (l.getElement().equals(pe)) {
                myLinks.add(l);
            } else if (l.getSwitch().equals(pe)) {
                myLinks.add(l);
            }
        }
        return myLinks;
    }

    /**
     * Get the full list of nodes that have been connected into the network
     *
     * @return the list of nodes
     */
    public List<Node> getConnectedNodes() {
        List<Node> nodes = new ArrayList<>();
        for (Link l : links) {
            if (l.getElement() instanceof Node) {
                nodes.add((Node) l.getElement());
            }
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
     * @throws IOException if an error occurred while writing
     */
    public void generateDot(Model mo, String out, boolean fromLeftToRight) throws IOException {

        try (BufferedWriter dot = new BufferedWriter(new FileWriter(out))) {
            dot.append("digraph G {\n");
            if (fromLeftToRight) {
                dot.append("rankdir=LR;\n");
            }
            drawNodes(dot, NamingService.getNodeNames(mo));
            drawSwitches(dot);
            drawLinks(dot);
            dot.append("}\n");
        }
    }

    private void drawNodes(BufferedWriter dot, NamingService<Node> nsNodes) throws IOException {
        for (Node n : getConnectedNodes()) {
            dot.append("node").
                    append(String.valueOf(n.id())).
                    append(" [shape=box, color=green, label=\"");
            if (nsNodes == null) {
                dot.append("Node ").append(String.valueOf(n.id()));
            } else {
                dot.append(nsNodes.resolve(n));
            }
            dot.append("\"];\n");
        }
    }

    private void drawSwitches(BufferedWriter dot) throws IOException {
        for (Switch s : switches) {
            dot.append("switch").
                    append(String.valueOf(s.id())).
                    append(" [shape=circle, color=blue, label=\"").
                    append("Switch ").append(String.valueOf(s.id()));
            if (s.getCapacity() > 0) {
                dot.append("\\n[").append(bitsToString(s.getCapacity())).append("/s]");
            }
            dot.append("\"];\n");
        }
    }

    private void drawLinks(BufferedWriter dot) throws IOException {
        for (Link l : links) {
            dot.append("switch").append(String.valueOf(l.getSwitch())).append(" -> ");
            dot.append(String.valueOf(l.getElement()));
            dot.append(" [arrowhead=none, color=red, label=\"").
                    append(bitsToString(l.getCapacity())).append("/s").
                    append("\"]\n");
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
     * Convert megabits into a more 'human readable' format (from 'long' to 'String')
     *
     * @param megabits  the amount in megabit (mb)
     * @return a String containing a pretty formatted output
     */
    private static String bitsToString(long megabits) {
        int unit = 1000;
        if (megabits < unit) {
            return megabits + " mb";
        }
        int exp = (int) (Math.log(megabits) / Math.log(unit));
        return new DecimalFormat("#.##").format(megabits / Math.pow(unit, exp)) + "GTPE".charAt(exp - 1) + "b";
    }

    /**
     * Create and attach a default network view to the given model.
     * Basically, the Network consists of a main non-blocking switch connected
     * to all the existing nodes in the model using 1Gbit/sec. links.
     *
     * Note: replace the previous Network view attached to the model (if exists).
     *
     * @param mo    the model to add/replace the Network view
     * @return the new 'default' network view, already attached to the given model
     */
    public static Network createDefaultNetwork(Model mo) {
        return createDefaultNetwork(mo, 1000);
    }

    /**
     * Create and attach a default network view to the given model.
     * Basically, the Network consists of a main non-blocking switch connected
     * to all the existing nodes in the model using links with the given
     * bandwidth in Mbit/sec.
     *
     * Note: replace the previous Network view attached to the model (if exists).
     *
     * @param mo    the model to add/replace the Network view
     * @param bw    the links bandwidth
     * @return the new 'default' network view, already attached to the given model
     */
    public static Network createDefaultNetwork(Model mo, int bw) {
        Network net = new Network();
        Switch mainSwitch = net.newSwitch(); // Main non-blocking switch
        for (Node n : mo.getMapping().getAllNodes()) {
            net.connect(bw, mainSwitch, n); // Connect all nodes with 1Gbit/s links
        }
        // Remove the current Network view if exists
        if (get(mo) != null) {
            mo.detach(get(mo));
        }
        mo.attach(net);
        return net;
    }

    @Override
    public String getIdentifier() {
        return VIEW_ID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return VIEW_ID.equals(((Network) o).getIdentifier());
    }

    @Override
    public ModelView copy() {
        Network net = new Network();
        net.routing = routing.copy();
        net.routing.setNetwork(net);
        net.switches.addAll(switches);
        net.links.addAll(links);
        net.lnBuilder = lnBuilder.copy();
        net.swBuilder = swBuilder.copy();
        return net;
    }

    /**
     * Get the network view associated to a model if exists.
     *
     * @param mo the model to look at
     * @return the network view if attached. {@code null} otherwise
     */
    public static Network get(Model mo) {
        return (Network) mo.getView(VIEW_ID);
    }

    @Override
    public boolean substituteVM(VM curId, VM nextId) {
        return false;
    }

    @Override
    public int hashCode() {
        return VIEW_ID.hashCode();
    }
}
