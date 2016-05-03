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

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.*;

import java.util.Map;

import static org.btrplace.json.JSONs.getNode;
import static org.btrplace.json.JSONs.getVM;


/**
 * Serialize/un-serialize attributes.
 * In practice, the JSON representation is a map where int are the keys.
 * For each of these keys, a map contains the key/values pair associated
 * to the element. A value is either a boolean ("true" or "false"), a number (integer or real), or a string.
 *
 * @author Fabien Hermenier
 */
public class AttributesConverter {

    private AttributesConverter() {
    }

    private static void putAttributes(Attributes attrs, Element e, JSONObject entries) {
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value.getClass().equals(Boolean.class)) {
                attrs.put(e, key, (Boolean) value);
            } else if (value.getClass().equals(String.class)) {
                attrs.put(e, key, (String) value);
            } else if (value.getClass().equals(Double.class)) {
                attrs.put(e, key, (Double) value);
            } else if (value.getClass().equals(Integer.class)) {
                attrs.put(e, key, (Integer) value);
            } else {
                throw new ClassCastException(value.toString() + " is not a primitive (" + value.getClass() + ")");
            }
        }
    }

    public static Attributes fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        Attributes attrs = new DefaultAttributes();
        try {

            JSONObject vms = (JSONObject) o.get("vms");
            if (vms != null) {
                for (Map.Entry<String, Object> e : vms.entrySet()) {
                    String el = e.getKey();
                    VM vm = getVM(mo, Integer.parseInt(el));
                    JSONObject entries = (JSONObject) e.getValue();
                    putAttributes(attrs, vm, entries);
                }
            }

            JSONObject nodes = (JSONObject) o.get("nodes");
            if (nodes != null) {
                for (Map.Entry<String, Object> e : nodes.entrySet()) {
                    String el = e.getKey();
                    Node n = getNode(mo, Integer.parseInt(el));
                    JSONObject entries = (JSONObject) e.getValue();
                    putAttributes(attrs, n, entries);
                }
            }
        } catch (ClassCastException ex) {
            throw new JSONConverterException(ex);
        }
        return attrs;
    }

    public static JSONObject toJSON(Attributes attributes) {
        JSONObject res = new JSONObject();
        JSONObject vms = new JSONObject();
        JSONObject nodes = new JSONObject();
        for (Element e : attributes.getDefined()) {
            JSONObject el = new JSONObject();
            for (String k : attributes.getKeys(e)) {
                el.put(k, attributes.get(e, k));
            }
            if (e instanceof VM) {
                vms.put(Integer.toString(e.id()), el);
            } else {
                nodes.put(Integer.toString(e.id()), el);
            }
        }
        res.put("vms", vms);
        res.put("nodes", nodes);
        return res;
    }
}
