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

    /**
     * Convert an array of int in the json format to a set.
     *
     * @param a the json array
     * @return the set of int
     */
    public static Set<Integer> elementsFromJSON(JSONArray a) {
        Set<Integer> s = new HashSet<>(a.size());
        for (Object o : a) {
            s.add((int) o);
        }
        return s;
    }

    /**
     * Convert an array of ints in the java format to a json array.
     *
     * @param s the collection of ints
     * @return the json formatted array of ints
     */
    public static JSONArray elementsToJSON(Collection<Integer> s) {
        JSONArray a = new JSONArray();
        for (int u : s) {
            a.add(u);
        }
        return a;
    }

    /**
     * Read an expected set of set of ints.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the set
     * @return the set
     * @throws btrplace.json.JSONConverterException
     *          if the key does not point to a set
     */
    public static Set<Set<Integer>> requiredSets(JSONObject o, String id) throws JSONConverterException {
        Set<Set<Integer>> res = new HashSet<>();
        Object x = o.get(id);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("Set of ints sets expected at key '" + id + "'");
        }
        for (Object obj : (JSONArray) o.get(id)) {
            res.add(elementsFromJSON((JSONArray) obj));
        }
        return res;
    }

    /**
     * Read an expected set of ints.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the set
     * @return the set
     * @throws btrplace.json.JSONConverterException
     *          if the key does not point to a set of ints
     */
    public static Set<Integer> requiredElements(JSONObject o, String id) throws JSONConverterException {
        Object x = o.get(id);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("Set of ints expected at key '" + id + "'");
        }
        return elementsFromJSON((JSONArray) x);
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
     * Read an expected long.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the long
     * @return the long
     * @throws btrplace.json.JSONConverterException
     *          if the key does not point to a long
     */
    public static long requiredLong(JSONObject o, String id) throws JSONConverterException {
        Object x = o.get(id);
        if (x == null) {
            throw new JSONConverterException("No value at key '" + id + "'");
        }
        if (!(x instanceof Number) || Math.ceil(((Number) x).doubleValue()) != ((Number) x).longValue()) {
            throw new JSONConverterException("Natural number expected at key '" + id + "' but was '" + x.getClass() + "'.");
        }
        return ((Number) x).longValue();
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

}
