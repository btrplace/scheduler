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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Basic abstract solver-API/JSON objects converter.
 *
 * @author Fabien Hermenier
 */
public abstract class JSONConverter<E> {

    /**
     * Convert an array of UUID in the json format to a set.
     *
     * @param a the json array
     * @return the set of UUID
     */
    public static Set<UUID> uuidsFromJSON(JSONArray a) {
        Set<UUID> s = new HashSet<>(a.size());
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
     * @throws btrplace.json.JSONConverterException
     *          if the key does not point to a set
     */
    public static Set<Set<UUID>> requiredSets(JSONObject o, String id) throws JSONConverterException {
        Set<Set<UUID>> res = new HashSet<>();
        Object x = o.get(id);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("Set of UUIDs sets expected at key '" + id + "'");
        }
        for (Object obj : (JSONArray) o.get(id)) {
            res.add(uuidsFromJSON((JSONArray) obj));
        }
        return res;
    }

    /**
     * Read an expected set of UUIDs.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the set
     * @return the set
     * @throws btrplace.json.JSONConverterException
     *          if the key does not point to a set of UUIDs
     */
    public static Set<UUID> requiredUUIDs(JSONObject o, String id) throws JSONConverterException {
        Object x = o.get(id);
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("Set of UUIDs expected at key '" + id + "'");
        }
        return uuidsFromJSON((JSONArray) x);
    }

    /**
     * Read an expected UUID.
     *
     * @param o  the object to parse
     * @param id the id in the map that should point to the UUID.
     * @return the UUID
     * @throws btrplace.json.JSONConverterException
     *          if the key does not point to a UUID
     */
    public static UUID requiredUUID(JSONObject o, String id) throws JSONConverterException {
        if (!o.containsKey(id)) {
            throw new JSONConverterException("No value at key '" + id + "'");
        }
        try {
            return UUID.fromString(o.get(id).toString());
        } catch (Exception e) {
            throw new JSONConverterException("Unable to read a UUID from string '" + id + "'", e);
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

    /**
     * JSON to Java object conversion
     *
     * @param in the json object
     * @return the conversion result
     * @throws JSONConverterException if an error occurred while converting the object
     */
    public abstract E fromJSON(JSONObject in) throws JSONConverterException;

    /**
     * Java to JSON conversion
     *
     * @param e the Java object to convert
     * @return the conversion result
     * @throws JSONConverterException if an error occurred while converting the object
     */
    public abstract JSONObject toJSON(E e) throws JSONConverterException;

    /**
     * Extract an object from a JSON object stored in a file.
     *
     * @param path the file path
     * @return the resulting object
     * @throws IOException            if an error occurred while reading the stream
     * @throws JSONConverterException if the stream cannot be parsed
     */
    public E fromJSONFile(String path) throws IOException, JSONConverterException {
        return fromJSON(new FileReader(path));
    }

    /**
     * Extract an object from a JSON object available on a stream.
     * The stream is closed afterward
     *
     * @param r the stream to read
     * @return the resulting JSONObject
     * @throws IOException            if an error occurred while reading the stream
     * @throws JSONConverterException if the stream cannot be parsed
     */
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
        } finally {
            r.close();
        }
    }

    /**
     * Write the object as a JSON message.
     *
     * @param o the object to write
     * @return the JSON message
     * @throws JSONConverterException if an error occurred while converting the object
     */
    public String toJSONString(E o) throws JSONConverterException {
        return toJSON(o).toJSONString();
    }

    /**
     * Append the JSON conversion of an object.
     *
     * @param e the object to write
     * @param w the stream where the JSON version of the object will be appended
     * @throws JSONConverterException if an error occurred while converting the object
     * @throws IOException            if an error occurred while writing the object
     */
    public void toJSON(E e, Appendable w) throws JSONConverterException, IOException {
        toJSON(e).writeJSONString(w);
    }

    /**
     * Serialize the object into a file using the JSON format.
     *
     * @param e    the object to write
     * @param path the path name
     * @throws JSONConverterException if an error occurred while converting the object
     * @throws IOException            if an error occurred while writing the object
     */
    public void toJSON(E e, String path) throws JSONConverterException, IOException {
        toJSON(e, new FileWriter(path));
    }
}
