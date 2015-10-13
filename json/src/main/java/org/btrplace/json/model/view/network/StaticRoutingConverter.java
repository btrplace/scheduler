package org.btrplace.json.model.view.network;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.view.network.Link;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.Routing;
import org.btrplace.model.view.network.StaticRouting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A converter to (un-)serialise a {@link StaticRouting}.
 *
 * All the routes are serialized.
 * @author Fabien Hermenier
 */
public class StaticRoutingConverter extends RoutingConverter<StaticRouting> {

    @Override
    public Class<StaticRouting> getSupportedRouting() {
        return StaticRouting.class;
    }

    /**
     * Return the routing identifier.
     *
     * @return {@value "static"}
     */
    @Override
    public String getJSONId() {
        return "static";
    }

    @Override
    public Routing fromJSON(JSONObject o) throws JSONConverterException {
        Model mo = getModel();
        Network v = (Network) mo.getView(Network.VIEW_ID);
        Map<Integer, Link> idToLink = new HashMap<>();
        for (Link l : v.getLinks()) {
            idToLink.put(l.id(), l);
        }
        StaticRouting r = new StaticRouting();
        checkKeys(o, "routes");
        JSONArray a = (JSONArray) o.get("routes");
        for (Object ao : a) {
            StaticRouting.NodesMap nm = nodesMapFromJSON((JSONObject) ((JSONObject) ao).get("nodes_map"));
            List<Link> links = new ArrayList<>();
            JSONArray aoa = (JSONArray) ((JSONObject) ao).get("links");
            for (Object aoao : aoa) {
                links.add(idToLink.get(aoao));
            }
            r.setStaticRoute(nm, links);
        }
        return r;
    }

    /**
     * Convert a JSON nodes map object into a Java NodesMap object
     *
     * @param o the JSON object to convert
     * @return the nodes map
     */
    public StaticRouting.NodesMap nodesMapFromJSON(JSONObject o) throws JSONConverterException {
        return new StaticRouting.NodesMap(requiredNode(o, "src"), requiredNode(o, "dst"));
    }

    /**
     * Convert a nodes map (a pair of two distinguishable nodes => source and destination) into a JSON object
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
     * @throws JSONConverterException if the Routing implementation is not known
     */
    @Override
    public JSONObject toJSON(Routing routing) throws JSONConverterException {
        JSONObject o = new JSONObject();
        o.put("type", getJSONId());
        JSONArray a = new JSONArray();
        for (StaticRouting.NodesMap nm : ((StaticRouting) routing).getStaticRoutes().keySet()) {
            JSONObject ao = new JSONObject();
            ao.put("nodes_map", nodesMapToJSON(nm));
            JSONArray links = new JSONArray();
            for (Link l : ((StaticRouting) routing).getStaticRoute(nm)) {
                links.add(l.id());
            }
            ao.put("links", links);
            a.add(ao);
        }
        o.put("routes", a);
        return o;
    }
}
