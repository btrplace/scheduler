/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace;


import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tools to help at converting object to JSON.
 *
 * @author Fabien Hermenier
 */
public final class Utils {

    private Utils() {
    }

    /**
     * Convert an array of UUID in the json format to a set.
     *
     * @param a the json array
     * @return the set of UUID
     */
    public static Set<UUID> fromJSON(JSONArray a) {
        Set<UUID> s = new HashSet<UUID>(a.size());
        for (Object o : a) {
            s.add(UUID.fromString((String) o));
        }
        return s;
    }

    /**
     * Convert an array of UUIDs in the java format to a json array.
     *
     * @param s the collection of UUIDs
     * @return the json formatted array of UUIDs
     */
    public static JSONArray toJSON(Collection<UUID> s) {
        JSONArray a = new JSONArray();
        for (UUID u : s) {
            a.add(u.toString());
        }
        return a;
    }

    /**
     * Read an expected set of set of UUIDs.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the set
     * @return the set
     * @throws JSONConverterException if the key does not point to a set
     */
    public static Set<Set<UUID>> requiredSets(JSONObject o, String id) throws JSONConverterException {
        Set<Set<UUID>> res = new HashSet<Set<UUID>>();
        Object x = o.get(id);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("Set of UUIDs sets expected at key '" + id + "'");
        }
        for (Object obj : (JSONArray) o.get(id)) {
            res.add(Utils.fromJSON((JSONArray) obj));
        }
        return res;
    }

    /**
     * Read an expected set of UUIDs.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the set
     * @return the set
     * @throws JSONConverterException if the key does not point to a set of UUIDs
     */
    public static Set<UUID> requiredUUIDs(JSONObject o, String id) throws JSONConverterException {
        Object x = o.get(id);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("Set of UUIDs expected at key '" + id + "'");
        }
        return fromJSON((JSONArray) x);
    }

    /**
     * Read an expected string.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the string
     * @return the string
     * @throws JSONConverterException if the key does not point to a string
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
     * @throws JSONConverterException if the key does not point to a long
     */
    public static long requiredLong(JSONObject o, String id) throws JSONConverterException {
        Object x = o.get(id);
        if (!(x instanceof Long)) {
            throw new JSONConverterException("Integer expected at key '" + id + "' but was '" + x.getClass() + "'.");
        }
        return (Long) x;
    }

    /**
     * Read an expected double.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the double
     * @return the double
     * @throws JSONConverterException if the key does not point to a double
     */
    public static double requiredDouble(JSONObject o, String id) throws JSONConverterException {
        Object x = o.get(id);
        if (!(x instanceof Double)) {
            throw new JSONConverterException("Real number expected at key '" + id + "' but was '" + x.getClass() + "'.");
        }
        return (Double) x;
    }

    /**
     * Read an expected boolean.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the boolean
     * @return the boolean
     * @throws JSONConverterException if the key does not point to a boolean
     */
    public static boolean requiredBoolean(JSONObject o, String id) throws JSONConverterException {
        Object x = o.get(id);
        if (!(x instanceof Boolean)) {
            throw new JSONConverterException("Boolean expected at key '" + id + "' but was '" + x.getClass() + "'.");
        }
        return (Boolean) x;
    }

    /**
     * Extract one JSON object from a string
     *
     * @param str the string to parse
     * @return the resulting JSONObject
     * @throws ParseException if an error occurred while parsing
     */
    public static JSONObject readObject(String str) throws ParseException, JSONConverterException {
        JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
        Object o = p.parse(str);
        if (!(o instanceof JSONObject)) {
            throw new JSONConverterException("Unable to parse a JSON object");
        }
        return (JSONObject) o;
    }

    /**
     * Extract one JSON object from a stream.
     * The stream is closed afterward
     *
     * @param in the stream to read
     * @return the resulting JSONObject
     * @throws ParseException if an error occurred while parsing
     */
    public static JSONObject readObject(Reader in) throws ParseException, JSONConverterException, IOException {
        try {
            JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
            Object o = p.parse(in);
            if (!(o instanceof JSONObject)) {
                throw new JSONConverterException("Unable to parse a JSON object");
            }
            return (JSONObject) o;
        } finally {
            in.close();
        }
    }
}
