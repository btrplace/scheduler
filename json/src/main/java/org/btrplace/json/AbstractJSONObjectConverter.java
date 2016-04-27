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
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.btrplace.model.Element;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Basic abstract scheduler-API/JSON objects converter.
 *
 * By default, the charset used to encode and decode string is {@link StandardCharsets#UTF_8}.
 * @author Fabien Hermenier
 */
public abstract class AbstractJSONObjectConverter<E> implements JSONObjectConverter<E> {

    private Model mo;

    /**
     * The charset to use by default.
     */
    private Charset charset = StandardCharsets.UTF_8;

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
     * Get the associated charset.
     *
     * @return the current charset
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Set the charset to use to encode/decode streams
     *
     * @param c the charset to use
     */
    public void setCharset(Charset c) {
        this.charset = c;
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
            s.add(getVM((int) o));
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
            s.add(getNode((int) o));
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
            VM v = getVM((Integer) i);
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
            s.add(getNode((Integer) i));
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
            return getVM((Integer) o.get(id));
        } catch (ClassCastException e) {
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
            return getNode((Integer) o.get(id));
        } catch (ClassCastException e) {
            throw new JSONConverterException("Unable to read a Node identifier from string at key '" + id + "'", e);
        }
    }

    @Override
    public E fromJSON(File path) throws JSONConverterException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path), charset))) {
            return fromJSON(in);
        } catch (IOException ex) {
            throw new JSONConverterException(ex);
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
     * The VM is already a part of the model.
     *
     * @param vmID the node identifier
     * @return the resulting VM
     * @throws JSONConverterException if there is no model, or if the VM is unknown.
     */

    public VM getVM(int vmID) throws JSONConverterException {
        if (mo == null) {
            throw new JSONConverterException("Unable to extract VMs without a model to use as a reference");
        }
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
     * @param nodeID the node identifier
     * @return the resulting node
     * @throws JSONConverterException if there is no model, or if the node is unknown.
     */
    public Node getNode(int nodeID) throws JSONConverterException {
        if (mo == null) {
            throw new JSONConverterException("Unable to extract Nodes without a model to use as a reference");
        }
        Node n = new Node(nodeID);
        if (!mo.contains(n)) {
            throw new JSONConverterException("Undeclared node '" + nodeID + "'");
        }
        return n;
    }

    @Override
    public String toJSONString(E o) throws JSONConverterException {
        return toJSON(o).toJSONString();
    }

    @Override
    public void toJSON(E e, Appendable w) throws JSONConverterException {
        try {
            toJSON(e).writeJSONString(w);
        } catch (IOException ex) {
            throw new JSONConverterException(ex);
        }
    }

    @Override
    public void toJSON(E e, File path) throws JSONConverterException {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), charset))) {
            toJSON(e, out);
        } catch (IOException ex) {
            throw new JSONConverterException(ex);
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
