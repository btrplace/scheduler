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
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SplitAmong;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * JSON converter for the {@link SplitAmong} constraint.
 *
 * @author Fabien Hermenier
 */
public class SplitAmongConverter extends ConstraintConverter<SplitAmong> {

    @Override
    public Class<SplitAmong> getSupportedConstraint() {
        return SplitAmong.class;
    }

    @Override
    public String getJSONId() {
        return "splitAmong";
    }

    @Override
    public SplitAmong fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);

        Set<Collection<Node>> nodes = new HashSet<>();
        Object x = o.get("pParts");
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("Set of ints sets expected at key 'vParts'");
        }
        for (Object obj : (JSONArray) x) {
            nodes.add(nodesFromJSON((JSONArray) obj));
        }

        Set<Collection<VM>> vms = new HashSet<>();
        x = o.get("vParts");
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("Set of ints sets expected at key 'vParts'");
        }
        for (Object obj : (JSONArray) x) {
            vms.add(vmsFromJSON((JSONArray) obj));
        }

        return new SplitAmong(vms, nodes, requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(SplitAmong o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());

        JSONArray vGroups = new JSONArray();
        for (Collection<VM> grp : o.getGroupsOfVMs()) {
            vGroups.add(vmsToJSON(grp));
        }

        JSONArray pGroups = new JSONArray();
        for (Collection<Node> grp : o.getGroupsOfNodes()) {
            pGroups.add(nodesToJSON(grp));
        }

        c.put("vParts", vGroups);
        c.put("pParts", pGroups);
        c.put("continuous", o.isContinuous());
        return c;
    }
}
