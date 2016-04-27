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

package org.btrplace.json.model.view.network;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.view.ModelViewConverter;
import org.btrplace.model.Node;
import org.btrplace.model.PhysicalElement;
import org.btrplace.model.view.network.Link;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.Routing;
import org.btrplace.model.view.network.Switch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Serialize/Un-serialize a {@link org.btrplace.model.view.network.Network} view.
 * <p>
 * By default, it also registers a {@link DefaultRoutingConverter} and a {@link StaticRoutingConverter}.
 *
 * @author Vincent Kherbache
 */
public class NetworkConverter extends ModelViewConverter<Network> {

    private List<RoutingConverter<? extends Routing>> routingConverters;

    /**
     * Make a new converter.
     */
    public NetworkConverter() {
        routingConverters = new ArrayList<>();
        routingConverters.add(new DefaultRoutingConverter());
        routingConverters.add(new StaticRoutingConverter());
    }

    /**
     * Register a routing converter.
     *
     * @param r the converter to register
     */
    public void register(RoutingConverter<? extends Routing> r) {
        routingConverters.add(r);
    }

    /**
     * Remove a routing converter.
     *
     * @param r the converter to remove
     * @return {@code true} iff it has been removed
     */
    public boolean unRegister(RoutingConverter<? extends Routing> r) {
        return routingConverters.remove(r);
    }

    @Override
    public JSONObject toJSON(Network net) throws JSONConverterException {
        JSONObject container = new JSONObject();
        container.put("id", getJSONId());
        container.put("switches", switchesToJSON(net.getSwitches()));
        container.put("links", linksToJSON(net.getLinks()));
        container.put("routing", routingToJSON(net.getRouting()));

        // TODO: manage possible custom LinkBuilder and SwitchBuilder implementations.

        return container;
    }

    @Override
    public Network fromJSON(JSONObject o) throws JSONConverterException {

        String id = requiredString(o, "id");
        // Create, setup, and return the Network

        if (!id.equals(getJSONId())) {
            return null;
        }

        Network net = new Network();

        switchesFromJSON(net, (JSONArray) o.get("switches"));
        linksFromJSON(net, (JSONArray) o.get("links"));

        getModel().attach(net);
        net.setRouting(routingFromJSON((JSONObject) o.get("routing")));
        getModel().detach(net);
        return net;
    }

    /**
     * Convert a Switch to a JSON object.
     *
     * @param s the switch to convert
     * @return the JSON object
     */
    public JSONObject switchToJSON(Switch s) {
        JSONObject o = new JSONObject();
        o.put("id", s.id());
        o.put("capacity", s.getCapacity());
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
     * @return the JSON object
     */
    public JSONObject physicalElementToJSON(PhysicalElement pe) throws JSONConverterException {
        JSONObject o = new JSONObject();
        if (pe instanceof Node) {
            o.put("type", "node");
            o.put("id", ((Node) pe).id());
        } else if (pe instanceof Switch) {
            o.put("type", "switch");
            o.put("id", ((Switch) pe).id());
        } else {
            throw new JSONConverterException("Unsupported physical element '" + pe.getClass().toString() + "'");
        }
        return o;
    }

    /**
     * Convert a Link to a JSON object.
     *
     * @param s the switch to convert
     * @return the JSON object
     */
    public JSONObject linkToJSON(Link s) throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("id", s.id());
        o.put("capacity", s.getCapacity());
        o.put("switch", s.getSwitch().id());
        o.put("physicalElement", physicalElementToJSON(s.getElement()));
        return o;
    }

    /**
     * Convert a collection of links to an array of JSON links objects.
     *
     * @param c the collection of Links
     * @return a JSON formatted array of Links
     */
    public JSONArray linksToJSON(Collection<Link> c) throws JSONConverterException {
        JSONArray a = new JSONArray();
        for (Link l : c) {
            a.add(linkToJSON(l));
        }
        return a;
    }

    /**
     * Convert a Routing implementation into a JSON object
     *
     * @param routing the routing implementation to convert
     * @return the JSON formatted routing object
     * @throws JSONConverterException if the Routing implementation is not known
     */
    public JSONObject routingToJSON(Routing routing) throws JSONConverterException {

        for (RoutingConverter<? extends Routing> c : routingConverters) {
            if (c.getSupportedRouting().equals(routing.getClass())) {
                return c.toJSON(routing);
            }
        }
        throw new JSONConverterException("No converter registered for routing '" + routing.getClass() + "'");
    }


    /**
     * Convert a JSON switch object to a Switch.
     *
     * @param o the json object
     * @return the Switch
     */
    public Switch switchFromJSON(JSONObject o) throws JSONConverterException {
        return new Switch(requiredInt(o, "id"), requiredInt(o, "capacity"));
    }

    /**
     * Convert a JSON array of switches to a Java List of switches.
     *
     * @param net the network to populate
     * @param a   the json array
     */
    public void switchesFromJSON(Network net, JSONArray a) throws JSONConverterException {
        for (Object o : a) {
            net.newSwitch(requiredInt((JSONObject) o, "id"), requiredInt((JSONObject) o, "capacity"));
        }
    }

    /**
     * Convert a JSON physical element object to a Java PhysicalElement object.
     *
     * @param o the JSON object to convert the physical element to convert
     * @return the PhysicalElement
     */
    public PhysicalElement physicalElementFromJSON(Network net, JSONObject o) throws JSONConverterException {
        String type = requiredString(o, "type");
        switch (type) {
            case "node":
                return requiredNode(o, "id");
            case "switch":
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
     * @param net the network to populate
     * @param o   the JSON object to convert
     */
    public void linkFromJSON(Network net, JSONObject o) throws JSONConverterException {
        net.connect(requiredInt(o, "id"),
                requiredInt(o, "capacity"),
                getSwitch(net, requiredInt(o, "switch")),
                physicalElementFromJSON(net, (JSONObject) o.get("physicalElement"))
        );
    }

    /**
     * Convert a JSON array of links to a Java List of links.
     *
     * @param net the network to populate
     * @param a   the json array
     */
    public void linksFromJSON(Network net, JSONArray a) throws JSONConverterException {
        for (Object o : a) {
            linkFromJSON(net, (JSONObject) o);
        }
    }


    /**
     * Convert a JSON routing object into the corresponding java Routing implementation.
     *
     * @param o the JSON object to convert
     * @throws JSONConverterException if the Routing implementation is not known
     */
    public Routing routingFromJSON(JSONObject o) throws JSONConverterException {

        String type = requiredString(o, "type");
        for (RoutingConverter<? extends Routing> r : routingConverters) {
            if (r.getJSONId().equals(type)) {
                r.setModel(getModel());
                return r.fromJSON(o);
            }
        }
        throw new JSONConverterException("No converter registered for routing type '" + type + "'");
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

