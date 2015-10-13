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

package org.btrplace.json.model.view;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Node;
import org.btrplace.model.PhysicalElement;
import org.btrplace.model.view.network.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Serialize/Un-serialize a {@link org.btrplace.model.view.network.Network} view.
 *
 * @author Vincent Kherbache
 */
public class NetworkConverter extends ModelViewConverter<Network> {

    @Override
    public Class<Network> getSupportedView() {
        return Network.class;
    }

    @Override
    public String getJSONId() {
        return "net";
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
        if (!id.equals(getJSONId())) { return null; }
        List<Switch> switches = switchesFromJSON((JSONArray)o.get("switches"));
        List<Link> links = linksFromJSON((JSONArray)o.get("links"));
        Routing routing = routingFromJSON((JSONObject)o.get("routing"));
        
        // Create, setup, and return the Network
        Network net = new Network(routing);
        net.getSwitches().addAll(switches);
        net.getLinks().addAll(links);
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
     * @return  the JSON object
     */
    public JSONObject physicalElementToJSON(PhysicalElement pe) throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("type", pe.getClass().getCanonicalName());
        if (pe.getClass().equals(Node.class)) {
            o.put("node", ((Node)pe).id());
        }
        else if (pe.getClass().equals(Switch.class)) {
            o.put("switch", switchToJSON((Switch)pe));
        }
        else {
            throw new JSONConverterException("Unsupported PhysicalElement '" + pe.getClass().toString() + "'");
        }
        return o;
    }

    /**
     * Convert a Link to a JSON object.
     *
     * @param s the switch to convert
     * @return  the JSON object
     */
    public JSONObject linkToJSON(Link s) throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("id", s.id());
        o.put("capacity", s.getCapacity());
        o.put("switch", switchToJSON(s.getSwitch()));
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
     * Convert a nodes map (a pair of two distinguishable nodes => source and destination) into a JSON object
     * @param nm    the nodes map to convert
     * @return  the nodes map JSON object
     */
    public JSONObject nodesMapToJSON(Routing.NodesMap nm) {
        JSONObject o = new JSONObject();
        o.put("src_node", nm.getSrc().id());
        o.put("dst_node", nm.getDst().id());
        return o;
    }

    /**
     * Convert a Routing implementation into a JSON object
     * 
     * @param routing   the routing implementation to convert
     * @return  the JSON formatted routing object
     * @throws  JSONConverterException if the Routing implementation is not known
     */
    public JSONObject routingToJSON(Routing routing) throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("type", routing.getClass().getCanonicalName());
        
        // Default routing, nothing to save
        if (routing.getClass().equals(DefaultRouting.class)) {}
        
        // Static routing, need to save all the static routes registered
        else if (routing.getClass().equals(StaticRouting.class)) {
            JSONArray a = new JSONArray();
            for (Routing.NodesMap nm :((StaticRouting) routing).getStaticRoutes().keySet()) {
                JSONObject ao = new JSONObject();
                ao.put("nodes_map", nodesMapToJSON(nm));
                ao.put("links", linksToJSON(((StaticRouting) routing).getStaticRoutes().get(nm)));
                a.add(ao);
            }
            o.put("routes", a);
        }
        else {
            throw new JSONConverterException("Unsupported Routing '" + routing.getClass().getCanonicalName() + "'");
        }
        return o;
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
     * @param a the json array
     * @return the set of switches
     */
    public List<Switch> switchesFromJSON(JSONArray a) throws JSONConverterException {
        List<Switch> switches = new ArrayList<>(a.size());
        for (Object o : a) {
            switches.add(switchFromJSON((JSONObject) o));
        }
        return switches;
    }

    /**
     * Convert a JSON physical element object to a Java PhysicalElement object.
     *
     * @param o the JSON object to convert the physical element to convert
     * @return  the PhysicalElement
     */
    public PhysicalElement physicalElementFromJSON(JSONObject o) throws JSONConverterException {

        // Get the class from 'type' field
        Class physicalElement = null;
        try {
            physicalElement = Class.forName(requiredString(o, "type"));
        } catch (ClassNotFoundException e) {
            throw new JSONConverterException("Unknown PhysicalElement class '" + requiredString(o, "type") + "'");
        }
        if (physicalElement == null)
            throw new JSONConverterException("Unknown PhysicalElement class '" + requiredString(o, "type") + "'");

        // It's a Node
        if (physicalElement.equals(Node.class)) {
            return getOrMakeNode(requiredInt(o, "node"));
        }
        // a Switch
        else if (physicalElement.equals(Switch.class)) {
            return switchFromJSON((JSONObject) o.get("switch"));
        }
        else {
            throw new JSONConverterException("PhysicalElement implementation class '" + physicalElement.toString() +
                    "' is not managed by the JSON converter.");
        }
    }

    /**
     * Convert a JSON link object into a Java Link object.
     *
     * @param   o the JSON object to convert
     * @return  the Link
     */
    public Link linkFromJSON(JSONObject o) throws JSONConverterException {

        Link l = new Link(requiredInt(o, "id"),
                          requiredInt(o, "capacity"),
                          switchFromJSON((JSONObject)o.get("switch")),
                          physicalElementFromJSON((JSONObject) o.get("physicalElement"))
        );
        
        return l;
    }

    /**
     * Convert a JSON array of links to a Java List of links.
     *
     * @param a the json array
     * @return the set of Links
     */
    public List<Link> linksFromJSON(JSONArray a) throws JSONConverterException {
        List<Link> links = new ArrayList<>(a.size());
        for (Object o : a) {
            links.add(linkFromJSON((JSONObject) o));
        }
        return links;
    }

    /**
     * Convert a JSON nodes map object into a Java NodesMap object
     * @param o the JSON object to convert
     * @return  the nodes map
     */
    public Routing.NodesMap nodesMapFromJSON(JSONObject o) throws JSONConverterException {
        return new Routing.NodesMap(requiredNode(o, "src_node"), requiredNode(o, "dst_node"));
    }

    /**
     * Convert a JSON routing object into the corresponding java Routing implementation
     *
     * @param   o the JSON object to convert
     * @return  the Routing implementation
     * @throws  JSONConverterException if the Routing implementation is not known
     */
    public Routing routingFromJSON(JSONObject o) throws JSONConverterException {

        // Get the class from 'type' field
        Class routing = null;
        try {
            routing = Class.forName(requiredString(o, "type"));
        } catch (ClassNotFoundException e) {
            throw new JSONConverterException("Unknown Routing class '" + requiredString(o, "type") + "'");
        }
        if (routing == null)
            throw new JSONConverterException("Unknown Routing class '" + requiredString(o, "type") + "'");

        // It's a DefaultRouting, nothing special to do.
        if (routing.equals(DefaultRouting.class)) { return new DefaultRouting(); }
        
        // It's a StaticRouting, import all the stored static routes
        else if (routing.equals(StaticRouting.class)) {
            StaticRouting staticRouting = new StaticRouting();
            JSONArray a = (JSONArray) o.get("routes");
            for (Object ao : a) {
                Routing.NodesMap nm = nodesMapFromJSON((JSONObject)((JSONObject)ao).get("nodes_map"));
                List<Link> links = linksFromJSON((JSONArray)((JSONObject)ao).get("links"));
                staticRouting.setStaticRoute(nm, links);
            }
            return staticRouting;
        }
        else {
            throw new JSONConverterException("Routing implementation class '" + routing.toString() +
                    "' is not managed by the JSON converter.");
        }
    }
}
