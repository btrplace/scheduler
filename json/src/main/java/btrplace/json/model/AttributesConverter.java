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

package btrplace.json.model;

import btrplace.json.JSONConverter;
import btrplace.model.Attributes;
import btrplace.model.DefaultAttributes;
import net.minidev.json.JSONObject;

import java.util.UUID;

/**
 * Serialize/un-serialize attributes.
 * In practice, the JSON representation is a hashmap where UUID are the keys.
 * For each of these keys, a hashmap contains the key/values pair associated
 * to the element. A value is either a boolean ("true" or "false"), a number (integer or real), or a string.
 *
 * @author Fabien Hermenier
 */
public class AttributesConverter extends JSONConverter<Attributes> {

    @Override
    public Attributes fromJSON(JSONObject o) {
        Attributes attrs = new DefaultAttributes();
        for (Object el : o.keySet()) {
            UUID u = UUID.fromString(el.toString());
            JSONObject entries = (JSONObject) o.get(el);
            for (Object entry : entries.keySet()) {
                Object value = entries.get(entry);
                if (value.getClass().equals(Boolean.class)) {
                    attrs.put(u, entry.toString(), (Boolean) value);
                } else if (value.getClass().equals(Long.class)) {
                    attrs.put(u, entry.toString(), (Long) value);
                } else if (value.getClass().equals(String.class)) {
                    attrs.put(u, entry.toString(), (String) value);
                } else if (value.getClass().equals(Double.class)) {
                    attrs.put(u, entry.toString(), (Double) value);
                } else if (value.getClass().equals(Integer.class)) {
                    attrs.put(u, entry.toString(), (Integer) value);
                } else {
                    throw new ClassCastException(value.toString() + " is not a basic type (" + value.getClass() + ")");
                }

            }
        }
        return attrs;
    }

    @Override
    public JSONObject toJSON(Attributes attributes) {
        JSONObject res = new JSONObject();
        for (UUID e : attributes.getElements()) {
            JSONObject el = new JSONObject();
            for (String k : attributes.getKeys(e)) {
                el.put(k, attributes.get(e, k));
            }
            res.put(e.toString(), el);
        }
        return res;
    }
}
