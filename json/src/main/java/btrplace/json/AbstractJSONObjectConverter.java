/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.json;

import btrplace.model.Element;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Basic abstract solver-API/JSON objects converter.
 *
 * @author Fabien Hermenier
 */
public abstract class AbstractJSONObjectConverter<E> implements JSONObjectConverter<E> {

    private Model mo;

    public AbstractJSONObjectConverter() {
        this(null);
    }

    public AbstractJSONObjectConverter(Model m) {
        this.mo = m;
    }

    public void setModel(Model m) {
        mo = m;
    }

    public Model getModel() {
        return mo;
    }

    /**
     * Convert an array of int in the json format to a set of VMs.
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
     * Convert an array of int in the json format to a set of nodes.
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
     * Convert an array of elements to an array of element identifiers (integers).
     *
     * @param s the collection of elements
     * @return a json formatted array of integers
     */
    public JSONArray vmsToJSON(Collection<VM> s) {
        JSONArray a = new JSONArray();
        for (Element e : s) {
            a.add(e.id());
        }
        return a;
    }

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
     * @param id the id in the map that should point to the set
     * @return the set
     * @throws btrplace.json.JSONConverterException
     *          if the key does not point to a set of ints
     */
    public Set<VM> requiredVMs(JSONObject o, String id) throws JSONConverterException {
        Object x = o.get(id);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("Set of ints expected at key '" + id + "'");
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
     * @param id the id in the map that should point to the set of nodes identifier (integers)
     * @return the set of nodes
     * @throws btrplace.json.JSONConverterException
     *          if the key does not point to an array of ints
     */
    public Set<Node> requiredNodes(JSONObject o, String id) throws JSONConverterException {
        Object x = o.get(id);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("Set of ints expected at key '" + id + "'");
        }
        Set<Node> s = new HashSet<>(((JSONArray) x).size());
        for (Object i : (JSONArray) x) {
            s.add(getOrMakeNode((Integer) i));
        }
        return s;

    }

    /**
     * Read an expected int.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the int.
     * @return the int
     * @throws btrplace.json.JSONConverterException
     *          if the key does not point to a int
     */
    public static int requiredInt(JSONObject o, String id) throws JSONConverterException {
        if (!o.containsKey(id)) {
            throw new JSONConverterException("No value at key '" + id + "'");
        }
        try {
            return (int) o.get(id);
        } catch (Exception e) {
            throw new JSONConverterException("Unable to read a int from string '" + id + "'", e);
        }
    }

    public VM requiredVM(JSONObject o, String id) throws JSONConverterException {
        if (!o.containsKey(id)) {
            throw new JSONConverterException("No value at key '" + id + "'");
        }
        try {
            return getOrMakeVM((Integer) o.get(id));
        } catch (Exception e) {
            throw new JSONConverterException("Unable to read a VM identifier from string at key '" + id + "'", e);
        }
    }

    public Node requiredNode(JSONObject o, String id) throws JSONConverterException {
        if (!o.containsKey(id)) {
            throw new JSONConverterException("No value at key '" + id + "'");
        }
        try {
            return getOrMakeNode((Integer) o.get(id));
        } catch (Exception e) {
            throw new JSONConverterException("Unable to read a Node identifier from string at key '" + id + "'", e);
        }
    }

    /**
     * Read an expected string.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the string
     * @return the string
     * @throws btrplace.json.JSONConverterException
     *          if the key does not point to a string
     */
    public static String requiredString(JSONObject o, String id) throws JSONConverterException {
        Object x = o.get(id);
        if (x == null) {
            throw new JSONConverterException("String expected at key '" + id + "'");
        }
        return x.toString();
    }

    /**
     * Read an expected double.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the double
     * @return the double
     * @throws btrplace.json.JSONConverterException
     *          if the key does not point to a double
     */
    public static double requiredDouble(JSONObject o, String id) throws JSONConverterException {
        Object x = o.get(id);
        if (x == null) {
            throw new JSONConverterException("No value at key '" + id + "'");
        }
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
     * @throws btrplace.json.JSONConverterException
     *          if the key does not point to a boolean
     */
    public static boolean requiredBoolean(JSONObject o, String id) throws JSONConverterException {
        Object x = o.get(id);
        if (x == null) {
            throw new JSONConverterException("No value at key '" + id + "'");
        }
        if (!(x instanceof Boolean)) {
            throw new JSONConverterException("Boolean expected at key '" + id + "' but was '" + x.getClass() + "'.");
        }
        return (Boolean) x;
    }

    @Override
    public E fromJSON(File path) throws IOException, JSONConverterException {
        try (FileReader in = new FileReader(path)) {
            return fromJSON(in);
        }
    }

    @Override
    public E fromJSON(String buf) throws IOException, JSONConverterException {
        try (StringReader in = new StringReader(buf)) {
            return fromJSON(in);
        }

    }

    @Override
    public E fromJSON(Reader r) throws IOException, JSONConverterException {
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

    public VM getOrMakeVM(int vmID) throws JSONConverterException {
        if (mo == null) {
            throw new JSONConverterException("Unable to extract VMs without a model to use as a reference");
        }
        for (VM v : mo.getVMs()) {
            if (v.id() == vmID) {
                return v;
            }
        }
        return mo.newVM(vmID);
    }

    public Node getOrMakeNode(int nodeID) throws JSONConverterException {
        if (mo == null) {
            throw new JSONConverterException("Unable to extract VMs without a model to use as a reference");
        }
        for (Node n : mo.getNodes()) {
            if (n.id() == nodeID) {
                return n;
            }
        }
        return mo.newNode(nodeID);
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

    public Integer toJSON(Element e) {
        return e.id();
    }
}
