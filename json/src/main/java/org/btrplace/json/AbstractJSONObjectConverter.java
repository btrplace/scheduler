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

package org.btrplace.json;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.btrplace.model.Element;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Basic abstract scheduler-API/JSON objects converter.
 *
 * @author Fabien Hermenier
 */
public abstract class AbstractJSONObjectConverter<E> implements JSONObjectConverter<E> {

    private Model mo;

    /**
     * New converters without any model as
     * a backend to get VMs and nodes identifiers.
     */
    public AbstractJSONObjectConverter() {
        this(null);
    }

    /**
     * New converter that rely on a given model
     * to access VMs and nodes identifiers.
     *
     * @param m the model to use
     */
    public AbstractJSONObjectConverter(Model m) {
        this.mo = m;
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
     * Get the model used to generate VM and nodes from identifiers
     *
     * @return a model, may be {@code null} if no identifiers have to be generated
     */
    public Model getModel() {
        return mo;
    }

    /**
     * Set the model to use to generate VMs and nodes from identifiers.
     *
     * @param m the model to use
     */
    public void setModel(Model m) {
        mo = m;
    }

    /**
     * Convert an array of VM identifiers to a set of VMs.
     *
     * @param a the json array
     * @return the set of VMs
     */
    public Set<VM> vmsFromJSON(JSONArray a) throws JSONConverterException {
        Set<VM> s = new HashSet<>(a.size());
        for (Object o : a) {
            s.add(getOrMakeVM((int) o));
        }
        return s;
    }

    /**
     * Convert an array of VM identifiers to a set of VMs.
     *
     * @param a the json array
     * @return the set of nodes
     */
    public Set<Node> nodesFromJSON(JSONArray a) throws JSONConverterException {
        Set<Node> s = new HashSet<>(a.size());
        for (Object o : a) {
            s.add(getOrMakeNode((int) o));
        }
        return s;
    }

    /**
     * Convert a collection of VMs to an array of VM identifiers.
     *
     * @param s the VMs
     * @return a json formatted array of integers
     */
    public JSONArray vmsToJSON(Collection<VM> s) {
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
    public JSONArray nodesToJSON(Collection<Node> s) {
        JSONArray a = new JSONArray();
        for (Element e : s) {
            a.add(e.id());
        }
        return a;
    }

    /**
     * Read an expected set of VMs.
     *
     * @param o  the object to parse
     * @param id the key in the map that points to the set
     * @return the parsed set
     * @throws JSONConverterException if the key does not point to a set of VM identifiers
     */
    public Set<VM> requiredVMs(JSONObject o, String id) throws JSONConverterException {
        checkKeys(o, id);
        Object x = o.get(id);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("integers expected at key '" + id + "'");
        }
        Set<VM> s = new HashSet<>(((JSONArray) x).size());
        for (Object i : (JSONArray) x) {
            VM v = getOrMakeVM((Integer) i);
            s.add(v);
        }
        return s;
    }

    /**
     * Read an expected set of nodes.
     *
     * @param o  the object to parse
     * @param id the key in the map that points to the set
     * @return the parsed set
     * @throws JSONConverterException if the key does not point to a set of nodes identifiers
     */
    public Set<Node> requiredNodes(JSONObject o, String id) throws JSONConverterException {
        checkKeys(o, id);
        Object x = o.get(id);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("integers expected at key '" + id + "'");
        }
        Set<Node> s = new HashSet<>(((JSONArray) x).size());
        for (Object i : (JSONArray) x) {
            s.add(getOrMakeNode((Integer) i));
        }
        return s;

    }

    /**
     * Read an expected VM.
     *
     * @param o  the object to parse
     * @param id the key in the map that points to the VM identifier
     * @return the VM
     * @throws JSONConverterException if the key does not point to a VM identifier
     */
    public VM requiredVM(JSONObject o, String id) throws JSONConverterException {
        checkKeys(o, id);
        try {
            return getOrMakeVM((Integer) o.get(id));
        } catch (Exception e) {
            throw new JSONConverterException("Unable to read a VM identifier from string at key '" + id + "'", e);
        }
    }

    /**
     * Read an expected node.
     *
     * @param o  the object to parse
     * @param id the key in the map that points to the node identifier
     * @return the node
     * @throws JSONConverterException if the key does not point to a node identifier
     */
    public Node requiredNode(JSONObject o, String id) throws JSONConverterException {
        checkKeys(o, id);
        try {
            return getOrMakeNode((Integer) o.get(id));
        } catch (Exception e) {
            throw new JSONConverterException("Unable to read a Node identifier from string at key '" + id + "'", e);
        }
    }

    @Override
    public E fromJSON(File path) throws IOException, JSONConverterException {
        try (FileReader in = new FileReader(path)) {
            return fromJSON(in);
        }
    }

    @Override
    public E fromJSON(String buf) throws JSONConverterException {
        try (StringReader in = new StringReader(buf)) {
            return fromJSON(in);
        }

    }

    @Override
    public E fromJSON(Reader r) throws JSONConverterException {
        try {
            JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
            Object o = p.parse(r);
            if (!(o instanceof JSONObject)) {
                throw new JSONConverterException("Unable to parse a JSON object");
            }
            return fromJSON((JSONObject) o);
        } catch (ParseException ex) {
            throw new JSONConverterException(ex);
        }
    }

    /**
     * Get a VM from its identifier.
     * If the VM is already a part of the model, it is reused.
     * Otherwise, a new VM is created
     *
     * @param vmID the VM identifier
     * @return the resulting VM
     * @throws JSONConverterException if there is no model.
     */
    public VM getOrMakeVM(int vmID) throws JSONConverterException {
        if (mo == null) {
            throw new JSONConverterException("Unable to extract VMs without a model to use as a reference");
        }
        mo.newVM(vmID);
        return new VM(vmID);
    }

    /**
     * Get a node from its identifier.
     * If the node is already a part of the model, it is reused.
     * Otherwise, a new node is created
     *
     * @param nodeID the node identifier
     * @return the resulting node
     * @throws JSONConverterException if there is no model.
     */
    public Node getOrMakeNode(int nodeID) throws JSONConverterException {
        if (mo == null) {
            throw new JSONConverterException("Unable to extract VMs without a model to use as a reference");
        }
        mo.newNode(nodeID);
        return new Node(nodeID);
    }

    @Override
    public String toJSONString(E o) throws JSONConverterException {
        return toJSON(o).toJSONString();
    }

    @Override
    public void toJSON(E e, Appendable w) throws JSONConverterException, IOException {
        toJSON(e).writeJSONString(w);
    }

    @Override
    public void toJSON(E e, File path) throws JSONConverterException, IOException {
        try (FileWriter out = new FileWriter(path)) {
            toJSON(e, out);
        }
    }

    /**
     * Get an element identifier.
     *
     * @param e the element
     * @return its identifier
     */
    public Integer toJSON(Element e) {
        return e.id();
    }
}
