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

package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.json.Utils;
import btrplace.model.constraint.Among;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.Set;
import java.util.UUID;

/**
 * JSON converter for the {@link Among} constraint.
 *
 * @author Fabien Hermenier
 */
public class AmongConverter extends SatConstraintConverter<Among> {

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
        checkId(o);
        return new Among(Utils.requiredUUIDs(o, "vms"),
                Utils.requiredSets(o, "nodes"),
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
