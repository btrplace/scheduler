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

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Node;
import org.btrplace.model.constraint.Among;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * JSON converter for the {@link Among} constraint.
 *
 * @author Fabien Hermenier
 */
public class AmongConverter extends ConstraintConverter<Among> {

    @Override
    public Class<Among> getSupportedConstraint() {
        return Among.class;
    }

    @Override
    public String getJSONId() {
        return "among";
    }

    @Override
    public Among fromJSON(JSONObject o) throws JSONConverterException {
        checkKeys(o, "parts");
        checkId(o);
        Set<Collection<Node>> nodes = new HashSet<>();
        Object x = o.get("parts");
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("Set of int sets expected at key 'parts'");
        }
        for (Object obj : (JSONArray) x) {
            nodes.add(nodesFromJSON((JSONArray) obj));
        }

        return new Among(requiredVMs(o, "vms"),
                nodes,
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Among o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", vmsToJSON(o.getInvolvedVMs()));
        JSONArray a = new JSONArray();
        for (Collection<Node> grp : o.getGroupsOfNodes()) {
            a.add(nodesToJSON(grp));
        }
        c.put("parts", a);
        c.put("continuous", o.isContinuous());
        return c;
    }
}
