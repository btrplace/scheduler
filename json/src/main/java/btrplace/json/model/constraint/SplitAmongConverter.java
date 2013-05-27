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

package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.constraint.SplitAmong;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.Set;


/**
 * JSON converter for the {@link SplitAmong} constraint.
 *
 * @author Fabien Hermenier
 */
public class SplitAmongConverter extends SatConstraintConverter<SplitAmong> {

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
        return new SplitAmong(requiredSets(o, "vms"),
                requiredSets(o, "nodes"),
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(SplitAmong o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());

        JSONArray vGroups = new JSONArray();
        for (Set<Integer> grp : o.getGroupsOfVMs()) {
            vGroups.add(elementsToJSON(grp));
        }

        JSONArray pGroups = new JSONArray();
        for (Set<Integer> grp : o.getGroupsOfNodes()) {
            pGroups.add(elementsToJSON(grp));
        }

        c.put("vms", vGroups);
        c.put("nodes", pGroups);
        c.put("continuous", o.isContinuous());
        return c;
    }
}
