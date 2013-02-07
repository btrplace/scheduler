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

package btrplace.model.constraint;

import btrplace.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JSON converter for the {@link Among} constraint.
 *
 * @author Fabien Hermenier
 */
public class AmongConverter implements SatConstraintConverter<Among> {

    @Override
    public Class<Among> getSupportedConstraint() {
        return Among.class;
    }

    @Override
    public String getJSONId() {
        return "among";
    }

    @Override
    public Among fromJSON(JSONObject o) {
        String id = o.get("id").toString();
        if (!id.equals(getJSONId())) {
            return null;
        }
        Set<Set<UUID>> pGroups = new HashSet<Set<UUID>>();
        for (Object obj : (JSONArray) o.get("nodes")) {
            pGroups.add(Utils.fromJSON((JSONArray) obj));
        }
        return new Among(Utils.fromJSON((JSONArray) o.get("vms")),
                pGroups,
                (Boolean) o.get("continuous"));
    }

    @Override
    public JSONObject toJSON(Among o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", Utils.toJSON(o.getInvolvedVMs()));
        JSONArray a = new JSONArray();
        for (Set<UUID> grp : o.getGroupsOfNodes()) {
            a.add(Utils.toJSON(grp));
        }
        c.put("nodes", a);
        c.put("continuous", o.isContinuous());
        return c;
    }
}
