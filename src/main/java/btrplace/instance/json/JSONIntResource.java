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

package btrplace.instance.json;

import btrplace.model.DefaultIntResource;
import btrplace.model.IntResource;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Set;
import java.util.UUID;

/**
 * Serialize/Un-serialize an {@link btrplace.model.IntResource}.
 *
 * @author Fabien Hermenier
 */
public class JSONIntResource {

    private JSONParser p;

    /**
     * New instance.
     */
    public JSONIntResource() {
        p = new JSONParser();
    }

    /**
     * Convert to a JSONObject
     *
     * @param rc the resource to serialize
     * @return the resulting object
     */
    public JSONObject toJSON(IntResource rc) {
        JSONObject o = new JSONObject();
        o.put("id", rc.identifier());
        Set<UUID> elems = rc.getDefined();
        JSONObject values = new JSONObject();
        for (UUID u : elems) {
            values.put(u, rc.get(u));
        }
        o.put("values", values);
        return o;
    }

    /**
     * Parse a JSON message to get a resource from a stream.
     *
     * @param r the stream.
     * @return the resulting resource or {@code null} in case of error
     * @throws IOException if an error occurred while reading the stream
     */
    public IntResource fromJSON(Reader r) throws IOException {
        try {
            JSONObject o = (JSONObject) p.parse(r);
            if (!o.containsKey("id") || !o.containsKey("values")) {
                return null;
            }
            IntResource rc = new DefaultIntResource((String) o.get("id"));
            JSONObject values = (JSONObject) o.get("values");
            for (Object k : values.keySet()) {
                UUID u = UUID.fromString(k.toString());
                int v = Integer.parseInt(values.get(k).toString());
                rc.set(u, v);
            }
            return rc;
        } catch (ParseException ex) {
            return null;
        } catch (ClassCastException ex) {
            return null;
        }
    }

    /**
     * Parse a JSON String to get a resource.
     *
     * @param s the string to parse
     * @return the resulting resource or {@code null} in case of error
     */
    public IntResource fromJSON(String s) {
        StringReader r = null;
        try {
            r = new StringReader(s);
            return fromJSON(r);
        } catch (IOException ex) {
            return null;
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }
}
