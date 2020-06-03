/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.view.network;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.view.network.Link;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.StaticRouting;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.btrplace.json.JSONs.checkKeys;
import static org.btrplace.json.JSONs.requiredInt;
import static org.btrplace.json.JSONs.requiredNode;
import static org.btrplace.json.JSONs.requiredString;

/**
 * A converter to (un-)serialise a {@link StaticRouting}.
 * <p>
 * All the routes are serialized.
 *
 * @author Fabien Hermenier
 */
public class StaticRoutingConverter implements RoutingConverter<StaticRouting> {

    public static final String ROUTES_LABEL = "routes";
    @Override
    public Class<StaticRouting> getSupportedRouting() {
        return StaticRouting.class;
    }

    /**
     * Return the routing identifier.
     *
     * @return {@code "static"}
     */
    @Override
    public String getJSONId() {
        return "static";
    }

    @Override
    public StaticRouting fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        Network v = Network.get(mo);
        TIntObjectMap<Link> idToLink = new TIntObjectHashMap<>();
        for (Link l : v.getLinks()) {
            idToLink.put(l.id(), l);
        }
        StaticRouting r = new StaticRouting();
        checkKeys(o, ROUTES_LABEL);
        JSONArray a = (JSONArray) o.get(ROUTES_LABEL);
        for (Object ao : a) {
            StaticRouting.NodesMap nm = nodesMapFromJSON(mo, (JSONObject) ((JSONObject) ao).get("nodes_map"));
            Map<Link, Boolean> links = new LinkedHashMap<>();
            JSONArray aoa = (JSONArray) ((JSONObject) ao).get("links");
            for (Object aoao : aoa) {
                links.put(idToLink.get(requiredInt((JSONObject)aoao, "link")),
                        Boolean.valueOf(requiredString((JSONObject) aoao, "direction")));
            }
            r.setStaticRoute(nm, links);
        }
        return r;
    }

    /**
     * Convert a JSON nodes map object into a Java NodesMap object
     *
     * @param mo the model we focus on
     * @param o the JSON object to convert
     * @return the nodes map
     * @throws JSONConverterException if a error occurred during the conversion
     */
    public StaticRouting.NodesMap nodesMapFromJSON(Model mo, JSONObject o) throws JSONConverterException {
        return new StaticRouting.NodesMap(requiredNode(mo, o, "src"), requiredNode(mo, o, "dst"));
    }

    /**
     * Convert a nodes map (a pair of two distinguishable nodes to source and destination) into a JSON object
     *
     * @param nm the nodes map to convert
     * @return the nodes map JSON object
     */
    public JSONObject nodesMapToJSON(StaticRouting.NodesMap nm) {
        JSONObject o = new JSONObject();
        o.put("src", nm.getSrc().id());
        o.put("dst", nm.getDst().id());
        return o;
    }

    /**
     * Convert a Routing implementation into a JSON object
     *
     * @param routing the routing implementation to convert
     * @return the JSON formatted routing object
     */
    @Override
    public JSONObject toJSON(StaticRouting routing) {
        JSONObject o = new JSONObject();
        o.put("type", getJSONId());
        JSONArray a = new JSONArray();
        Map<StaticRouting.NodesMap, Map<Link, Boolean>> routes = routing.getStaticRoutes();
        for (Map.Entry<StaticRouting.NodesMap, Map<Link, Boolean>> e : routes.entrySet()) {
            StaticRouting.NodesMap nm = e.getKey();
            JSONObject ao = new JSONObject();
            ao.put("nodes_map", nodesMapToJSON(nm));
            JSONArray links = new JSONArray();
            Map<Link, Boolean> v = e.getValue();
            for (Link l : v.keySet()) {
                JSONObject lo = new JSONObject();
                lo.put("link", l.id());
                lo.put("direction", routes.get(nm).get(l).toString());
                links.add(lo);
            }
            ao.put("links", links);
            a.add(ao);
        }
        o.put(ROUTES_LABEL, a);
        return o;
    }
}
