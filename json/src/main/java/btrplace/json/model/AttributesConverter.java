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

package btrplace.json.model;

import btrplace.json.AbstractJSONObjectConverter;
import btrplace.json.JSONConverterException;
import btrplace.model.*;
import net.minidev.json.JSONObject;


/**
 * Serialize/un-serialize attributes.
 * In practice, the JSON representation is a map where int are the keys.
 * For each of these keys, a map contains the key/values pair associated
 * to the element. A value is either a boolean ("true" or "false"), a number (integer or real), or a string.
 *
 * @author Fabien Hermenier
 */
public class AttributesConverter extends AbstractJSONObjectConverter<Attributes> {

    private void putAttributes(Attributes attrs, Element e, JSONObject entries) {
        for (String key : entries.keySet()) {
            Object value = entries.get(key);
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

    @Override
    public Attributes fromJSON(JSONObject o) throws JSONConverterException {
        Attributes attrs = new DefaultAttributes();
        try {

            JSONObject vms = (JSONObject) o.get("vms");
            if (vms != null) {
                for (String el : vms.keySet()) {
                    VM vm = getOrMakeVM(Integer.parseInt(el));
                    JSONObject entries = (JSONObject) vms.get(el);
                    putAttributes(attrs, vm, entries);
                }
            }

            JSONObject nodes = (JSONObject) o.get("nodes");
            if (nodes != null) {
                for (String el : nodes.keySet()) {
                    Node n = getOrMakeNode(Integer.parseInt(el));
                    JSONObject entries = (JSONObject) nodes.get(el);
                    putAttributes(attrs, n, entries);
                }
            }
        } catch (ClassCastException ex) {
            throw new JSONConverterException(ex);
        }
        return attrs;
    }

    @Override
    public JSONObject toJSON(Attributes attributes) {
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
