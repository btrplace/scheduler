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

package org.btrplace.json;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.model.Element;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A helper class to ease JSON conversion.
 *
 * @author Fabien Hermenier
 */
public class JSONs {

    private JSONs() {
    }

    /**
     * Read an expected integer.
     *
     * @param o  the object to parse
     * @param id the key in the map that points to an integer
     * @return the int
     * @throws JSONConverterException if the key does not point to a int
     */
    public static int requiredInt(JSONObject o, String id) throws JSONConverterException {
        checkKeys(o, id);
        try {
            return (Integer) o.get(id);
        } catch (Exception e) {
            throw new JSONConverterException("Unable to read a int from string '" + id + "'", e);
        }
    }

    /**
     * Read an optional integer.
     *
     * @param o  the object to parse
     * @param id the key in the map that points to an integer
     * @param def the default integer value if the key is absent
     * @return the resulting integer
     * @throws JSONConverterException if the key does not point to a int
     */
    public static int optInt(JSONObject o, String id, int def) throws JSONConverterException {
        if (o.containsKey(id)) {
            try {
                return (Integer) o.get(id);
            } catch (Exception e) {
                throw new JSONConverterException("Unable to read a int from string '" + id + "'", e);
            }
        }
        return def;
    }

    /**
     * Check if some keys are present.
     *
     * @param o    the object to parse
     * @param keys the keys to check
     * @throws JSONConverterException when at least a key is missing
     */
    public static void checkKeys(JSONObject o, String... keys) throws JSONConverterException {
        for (String k : keys) {
            if (!o.containsKey(k)) {
                throw new JSONConverterException("Missing key '" + k + "'");
            }
        }
    }

    /**
     * Read an expected string.
     *
     * @param o  the object to parse
     * @param id the key in the map that points to the string
     * @return the string
     * @throws JSONConverterException if the key does not point to a string
     */
    public static String requiredString(JSONObject o, String id) throws JSONConverterException {
        checkKeys(o, id);
        Object x = o.get(id);
        return x.toString();
    }

    /**
     * Read an expected double.
     *
     * @param o  the object to parse
     * @param id the key in the map that points to the double
     * @return the double
     * @throws JSONConverterException if the key does not point to a double
     */
    public static double requiredDouble(JSONObject o, String id) throws JSONConverterException {
        checkKeys(o, id);
        Object x = o.get(id);
        if (!(x instanceof Number)) {
            throw new JSONConverterException("Number expected at key '" + id + "' but was '" + x.getClass() + "'.");
        }
        return ((Number) x).doubleValue();
    }

    /**
     * Read an expected boolean.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the boolean
     * @return the boolean
     * @throws org.btrplace.json.JSONConverterException if the key does not point to a boolean
     */
    public static boolean requiredBoolean(JSONObject o, String id) throws JSONConverterException {
        checkKeys(o, id);
        Object x = o.get(id);
        if (!(x instanceof Boolean)) {
            throw new JSONConverterException("Boolean expected at key '" + id + "' but was '" + x.getClass() + "'.");
        }
        return (Boolean) x;
    }

    /**
     * Convert an array of VM identifiers to a set of VMs.
     *
     * @param mo the associated model to browse
     * @param a the json array
     * @return the set of VMs
     */
    public static Set<VM> vmsFromJSON(Model mo, JSONArray a) throws JSONConverterException {
        Set<VM> s = new HashSet<>(a.size());
        for (Object o : a) {
            s.add(getVM(mo, (int) o));
        }
        return s;
    }

    /**
     * Convert an array of VM identifiers to a set of VMs.
     *
     * @param mo the associated model to browse
     * @param a the json array
     * @return the set of nodes
     */
    public static Set<Node> nodesFromJSON(Model mo, JSONArray a) throws JSONConverterException {
        Set<Node> s = new HashSet<>(a.size());
        for (Object o : a) {
            s.add(getNode(mo, (int) o));
        }
        return s;
    }

    /**
     * Convert a collection of VMs to an array of VM identifiers.
     *
     * @param s the VMs
     * @return a json formatted array of integers
     */
    public static JSONArray vmsToJSON(Collection<VM> s) {
        JSONArray a = new JSONArray();
        for (Element e : s) {
            a.add(e.id());
        }
        return a;
    }

    /**
     * Convert a collection nodes to an array of nodes identifiers.
     *
     * @param s the VMs
     * @return a json formatted array of integers
     */
    public static JSONArray nodesToJSON(Collection<Node> s) {
        JSONArray a = new JSONArray();
        for (Element e : s) {
            a.add(e.id());
        }
        return a;
    }

    /**
     * Read an expected set of VMs.
     *
     * @param mo the associated model to browse
     * @param o  the object to parse
     * @param id the key in the map that points to the set
     * @return the parsed set
     * @throws JSONConverterException if the key does not point to a set of VM identifiers
     */
    public static Set<VM> requiredVMs(Model mo, JSONObject o, String id) throws JSONConverterException {
        checkKeys(o, id);
        Object x = o.get(id);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("integers expected at key '" + id + "'");
        }
        Set<VM> s = new HashSet<>(((JSONArray) x).size());
        for (Object i : (JSONArray) x) {
            VM v = getVM(mo, (Integer) i);
            s.add(v);
        }
        return s;
    }

    /**
     * Read an expected set of nodes.
     *
     * @param mo the associated model to browse
     * @param o  the object to parse
     * @param id the key in the map that points to the set
     * @return the parsed set
     * @throws JSONConverterException if the key does not point to a set of nodes identifiers
     */
    public static Set<Node> requiredNodes(Model mo, JSONObject o, String id) throws JSONConverterException {
        checkKeys(o, id);
        Object x = o.get(id);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("integers expected at key '" + id + "'");
        }
        Set<Node> s = new HashSet<>(((JSONArray) x).size());
        for (Object i : (JSONArray) x) {
            s.add(getNode(mo, (Integer) i));
        }
        return s;

    }

    /**
     * Read an expected VM.
     *
     * @param mo the associated model to browse
     * @param o  the object to parse
     * @param id the key in the map that points to the VM identifier
     * @return the VM
     * @throws JSONConverterException if the key does not point to a VM identifier
     */
    public static VM requiredVM(Model mo, JSONObject o, String id) throws JSONConverterException {
        checkKeys(o, id);
        try {
            return getVM(mo, (Integer) o.get(id));
        } catch (ClassCastException e) {
            throw new JSONConverterException("Unable to read a VM identifier from string at key '" + id + "'", e);
        }
    }

    /**
     * Read an expected node.
     *
     * @param mo the associated model to browse
     * @param o  the object to parse
     * @param id the key in the map that points to the node identifier
     * @return the node
     * @throws JSONConverterException if the key does not point to a node identifier
     */
    public static Node requiredNode(Model mo, JSONObject o, String id) throws JSONConverterException {
        checkKeys(o, id);
        try {
            return getNode(mo, (Integer) o.get(id));
        } catch (ClassCastException e) {
            throw new JSONConverterException("Unable to read a Node identifier from string at key '" + id + "'", e);
        }
    }

    /**
     * Get a VM from its identifier.
     * The VM is already a part of the model.
     *
     * @param mo the associated model to browse
     * @param vmID the node identifier
     * @return the resulting VM
     * @throws JSONConverterException if there is no model, or if the VM is unknown.
     */
    public static VM getVM(Model mo, int vmID) throws JSONConverterException {
        VM vm = new VM(vmID);
        if (!mo.contains(vm)) {
            throw new JSONConverterException("Undeclared vm '" + vmID + "'");
        }
        return vm;
    }

    /**
     * Get a node from its identifier.
     * The node is already a part of the model
     *
     * @param mo the associated model to browse
     * @param nodeID the node identifier
     * @return the resulting node
     * @throws JSONConverterException if there is no model, or if the node is unknown.
     */
    public static Node getNode(Model mo, int nodeID) throws JSONConverterException {
        Node n = new Node(nodeID);
        if (!mo.contains(n)) {
            throw new JSONConverterException("Undeclared node '" + nodeID + "'");
        }
        return n;
    }

    /**
     * Get an element identifier.
     *
     * @param e the element
     * @return its identifier
     */
    public static Integer elementToJSON(Element e) {
        return e.id();
    }
}
