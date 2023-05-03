/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.view.network;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.view.ModelViewConverter;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.PhysicalElement;
import org.btrplace.model.view.network.Link;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.Routing;
import org.btrplace.model.view.network.Switch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.btrplace.json.JSONs.checkKeys;
import static org.btrplace.json.JSONs.requiredInt;
import static org.btrplace.json.JSONs.requiredNode;
import static org.btrplace.json.JSONs.requiredString;

/**
 * Serialize/Un-serialize a {@link org.btrplace.model.view.network.Network} view.
 * <p>
 * By default, it also registers a {@link DefaultRoutingConverter} and a {@link StaticRoutingConverter}.
 *
 * @author Vincent Kherbache
 */
public class NetworkConverter implements ModelViewConverter<Network> {

    private final Map<Class<? extends Routing>, RoutingConverter<? extends Routing>> java2json;
    private final Map<String, RoutingConverter<? extends Routing>> json2java;

    /**
     * Label stating a switch.
     */
    public static final String SWITCH_LABEL = "switch";

    public static final String SWITCHES_LABEL = "switches";

    public static final String LINKS_LABEL = "links";

    public static final String ROUTING_LABEL = "routing";
    /**
     * Label stating a node.
     */
    public static final String NODE_LABEL = "node";

    /**
     * Label stating a capacity.
     */
    public static final String CAPACITY_LABEL = "capacity";
    /**
     * Make a new converter.
     */
    public NetworkConverter() {
        java2json = new HashMap<>();
        json2java = new HashMap<>();
        register(new DefaultRoutingConverter());
        register(new StaticRoutingConverter());
    }

    /**
     * Register a routing converter.
     *
     * @param r the converter to register
     */
    public void register(RoutingConverter<? extends Routing> r) {
        java2json.put(r.getSupportedRouting(), r);
        json2java.put(r.getJSONId(), r);
    }

    @Override
    public JSONObject toJSON(Network net) throws JSONConverterException {
        JSONObject container = new JSONObject();
        container.put(ModelViewConverter.IDENTIFIER, getJSONId());
        container.put(SWITCHES_LABEL, switchesToJSON(net.getSwitches()));
        container.put(LINKS_LABEL, linksToJSON(net.getLinks()));
        container.put(ROUTING_LABEL, routingToJSON(net.getRouting()));

        return container;
    }

    @Override
    public Network fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkKeys(o, IDENTIFIER, SWITCHES_LABEL, LINKS_LABEL, ROUTING_LABEL);
        String id = requiredString(o, ModelViewConverter.IDENTIFIER);

        if (!id.equals(getJSONId())) {
            return null;
        }

        Network net = new Network();

        switchesFromJSON(net, (JSONArray) o.get(SWITCHES_LABEL));
        linksFromJSON(mo, net, (JSONArray) o.get(LINKS_LABEL));

        mo.attach(net);
        net.setRouting(routingFromJSON(mo, (JSONObject) o.get(ROUTING_LABEL)));
        mo.detach(net);
        return net;
    }

    /**
     * Convert a Switch to a JSON object.
     *
     * @param s the switch to convert
     * @return  the JSON object
     */
    public JSONObject switchToJSON(Switch s) {
        JSONObject o = new JSONObject();
        o.put("id", s.id());
        o.put(CAPACITY_LABEL, s.getCapacity());
        return o;
    }

    /**
     * Convert a collection of switches to an array of JSON switches objects.
     *
     * @param c the collection of Switches
     * @return a json formatted array of Switches
     */
    public JSONArray switchesToJSON(Collection<Switch> c) {
        JSONArray a = new JSONArray();
        for (Switch s : c) {
            a.add(switchToJSON(s));
        }
        return a;
    }

    /**
     * Convert a PhysicalElement to a JSON object.
     *
     * @param pe the physical element to convert
     * @return  the JSON object
     * @throws IllegalArgumentException if the physical element is not supported
     */
    public JSONObject physicalElementToJSON(PhysicalElement pe) {
        JSONObject o = new JSONObject();
        if (pe instanceof Node) {
            o.put("type", NODE_LABEL);
            o.put("id", ((Node) pe).id());
        } else if (pe instanceof Switch) {
            o.put("type", SWITCH_LABEL);
            o.put("id", ((Switch) pe).id());
        } else {
            throw new IllegalArgumentException("Unsupported physical element '" + pe.getClass().toString() + "'");
        }
        return o;
    }

    /**
     * Convert a Link to a JSON object.
     *
     * @param s the switch to convert
     * @return  the JSON object
     */
    public JSONObject linkToJSON(Link s) {
        JSONObject o = new JSONObject();
        o.put("id", s.id());
        o.put(CAPACITY_LABEL, s.getCapacity());
        o.put(SWITCH_LABEL, s.getSwitch().id());
        o.put("physicalElement", physicalElementToJSON(s.getElement()));
        return o;
    }

    /**
     * Convert a collection of links to an array of JSON links objects.
     *
     * @param c the collection of Links
     * @return a JSON formatted array of Links
     */
    public JSONArray linksToJSON(Collection<Link> c) {
        JSONArray a = new JSONArray();
        for (Link l : c) {
            a.add(linkToJSON(l));
        }
        return a;
    }

    /**
     * Convert a Routing implementation into a JSON object
     * 
     * @param routing   the routing implementation to convert
     * @return  the JSON formatted routing object
     * @throws  JSONConverterException if the Routing implementation is not known
     */
    public JSONObject routingToJSON(Routing routing) throws JSONConverterException {
        RoutingConverter c = java2json.get(routing.getClass());
        if (c == null) {
            throw new JSONConverterException("No converter available for a routing with the '" + routing.getClass() + "' className");
        }
        return c.toJSON(routing);

    }

    /**
     * Convert a JSON routing object into the corresponding java Routing implementation.
     *
     * @param mo the model we focus on
     * @param o the JSON object to convert
     * @return the resulting {@link Routing} object
     * @throws JSONConverterException if the Routing implementation is not known
     */
    public Routing routingFromJSON(Model mo, JSONObject o) throws JSONConverterException {

        String type = requiredString(o, "type");
        RoutingConverter<? extends Routing> c = json2java.get(type);
        if (c == null) {
            throw new JSONConverterException("No converter available for a routing of type '" + type + "'");
        }
        return c.fromJSON(mo, o);
    }

    /**
     * Convert a JSON switch object to a Switch.
     *
     * @param o the json object
     * @return the Switch
     * @throws JSONConverterException if an error occurred during conversion
     */
    public Switch switchFromJSON(JSONObject o) throws JSONConverterException {
        return new Switch(requiredInt(o, "id"), readCapacity(o));
    }

    /**
     * Convert a JSON array of switches to a Java List of switches.
     * @param net the network to populate
     * @param a the json array
     * @throws JSONConverterException if an error occurred during conversion
     */
    public void switchesFromJSON(Network net, JSONArray a) throws JSONConverterException {
        for (Object o : a) {
            net.newSwitch(requiredInt((JSONObject) o, "id"), readCapacity((JSONObject) o));
        }
    }

    private static int readCapacity(JSONObject o) throws JSONConverterException {
        int i = requiredInt(o, CAPACITY_LABEL);
        if (i < 0) {
            i = Integer.MAX_VALUE;
        }
        return i;
    }

    /**
     * Convert a JSON physical element object to a Java PhysicalElement object.
     *
     * @param mo the model we focus on
     * @param net the network view we manipulate
     * @param o the JSON object to convert the physical element to convert
     * @return  the PhysicalElement
     * @throws JSONConverterException if an error occurred during conversion
     */
    public PhysicalElement physicalElementFromJSON(Model mo, Network net, JSONObject o) throws JSONConverterException {
        String type = requiredString(o, "type");
        switch (type) {
            case NODE_LABEL:
                return requiredNode(mo, o, "id");
            case SWITCH_LABEL:
                return getSwitch(net, requiredInt(o, "id"));
            default:
                throw new JSONConverterException("type '" + type + "' is not a physical element");
        }
    }

    private static Switch getSwitch(Network net, int id) {
        for (Switch s : net.getSwitches()) {
            if (s.id() == id) {
                return s;
            }
        }
        return null;
    }
    /**
     * Convert a JSON link object into a Java Link object.
     *
     * @param mo the model we focus on
     * @param net the network to populate
     * @param   o the JSON object to convert
     * @throws JSONConverterException if an error occurred during conversion
     */
    public void linkFromJSON(Model mo, Network net, JSONObject o) throws JSONConverterException {
        net.connect(requiredInt(o, "id"),
                readCapacity(o),
                getSwitch(net, requiredInt(o, SWITCH_LABEL)),
                physicalElementFromJSON(mo, net, (JSONObject) o.get("physicalElement"))
        );
    }

    /**
     * Convert a JSON array of links to a Java List of links.
     *
     * @param mo the model we focus on
     * @param net the network to populate
     * @param a the json array
     * @throws JSONConverterException if an error occurred during conversion
     */
    public void linksFromJSON(Model mo, Network net, JSONArray a) throws JSONConverterException {
        for (Object o : a) {
            linkFromJSON(mo, net, (JSONObject) o);
        }
    }

    @Override
    public Class<Network> getSupportedView() {
        return Network.class;
    }

    @Override
    public String getJSONId() {
        return "net";
    }

}

