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

package org.btrplace.json.model;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.AbstractJSONObjectConverter;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Class to serialize and un-serialize {@link Mapping}.
 *
 * @author Fabien Hermenier
 */
public class MappingConverter extends AbstractJSONObjectConverter<Mapping> {

    @Override
    public JSONObject toJSON(Mapping c) {
        JSONObject o = new JSONObject();
        o.put("offlineNodes", nodesToJSON(c.getOfflineNodes()));
        o.put("readyVMs", vmsToJSON(c.getReadyVMs()));

        JSONObject ons = new JSONObject();
        for (Node n : c.getOnlineNodes()) {
            JSONObject w = new JSONObject();
            w.put("runningVMs", vmsToJSON(c.getRunningVMs(n)));
            w.put("sleepingVMs", vmsToJSON(c.getSleepingVMs(n)));
            ons.put(Integer.toString(n.id()), w);
        }
        o.put("onlineNodes", ons);
        return o;
    }

    @Override
    public Mapping fromJSON(JSONObject o) throws JSONConverterException {
        Model mo = getModel();
        if (mo == null) {
            throw new JSONConverterException("Unable to extract VMs without a model to use as a reference");
        }
        Mapping c = getModel().getMapping();
        for (Node u : newNodes(mo, o, "offlineNodes")) {
            c.addOfflineNode(u);
        }
        for (VM u : newVMs(mo, o, "readyVMs")) {
            c.addReadyVM(u);
        }
        JSONObject ons = (JSONObject) o.get("onlineNodes");
        for (Map.Entry<String, Object> e : ons.entrySet()) {
            int id = Integer.parseInt(e.getKey());
            Node u = mo.newNode(id);
            if (u == null) {
                throw new JSONConverterException("Node '" + id + "' already declared");
            }
            JSONObject on = (JSONObject) e.getValue();
            c.addOnlineNode(u);
            for (VM vm : newVMs(mo, on, "runningVMs")) {
                c.addRunningVM(vm, u);
            }
            for (VM vm : newVMs(mo, on, "sleepingVMs")) {
                c.addSleepingVM(vm, u);
            }
        }

        return c;
    }

    /**
     * Build nodes from a key.
     *
     * @param mo  the model to build
     * @param o   the object that contains the node
     * @param key the key associated to the nodes
     * @return the resulting set of nodes
     * @throws JSONConverterException if at least one of the parsed node already exists
     */
    private Set<Node> newNodes(Model mo, JSONObject o, String key) throws JSONConverterException {
        checkKeys(o, key);
        Object x = o.get(key);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("array expected at key '" + key + "'");
        }
        Set<Node> s = new HashSet<>(((JSONArray) x).size());
        for (Object i : (JSONArray) x) {
            int id = (Integer) i;
            Node n = mo.newNode(id);
            if (n == null) {
                throw new JSONConverterException("Node '" + id + "' already declared");
            }
            s.add(n);
        }
        return s;
    }

    /**
     * Build VMs from a key.
     *
     * @param mo  the model to build
     * @param o   the object that contains the vm
     * @param key the key associated to the VMs
     * @return the resulting set of VMs
     * @throws JSONConverterException if at least one of the parsed VM already exists
     */
    private Set<VM> newVMs(Model mo, JSONObject o, String key) throws JSONConverterException {
        checkKeys(o, key);
        Object x = o.get(key);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("array expected at key '" + key + "'");
        }

        Set<VM> s = new HashSet<>(((JSONArray) x).size());
        for (Object i : (JSONArray) x) {
            int id = (Integer) i;
            VM vm = mo.newVM(id);
            if (vm == null) {
                throw new JSONConverterException("VM '" + id + "' already declared");
            }
            s.add(vm);
        }
        return s;
    }
}
