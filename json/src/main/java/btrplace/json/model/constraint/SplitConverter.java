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
import btrplace.json.JSONUtils;
import btrplace.model.constraint.Split;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.Set;
import java.util.UUID;

/**
 * JSON converter for the {@link btrplace.model.constraint.Split} constraint.
 *
 * @author Fabien Hermenier
 */
public class SplitConverter extends SatConstraintConverter<Split> {

    @Override
    public Class<Split> getSupportedConstraint() {
        return Split.class;
    }

    @Override
    public String getJSONId() {
        return "split";
    }

    @Override
    public Split fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Split(JSONUtils.requiredSets(o, "vms"), JSONUtils.requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Split o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());

        JSONArray a = new JSONArray();
        for (Set<UUID> grp : o.getSets()) {
            a.add(JSONUtils.toJSON(grp));
        }

        c.put("vms", a);
        c.put("continuous", o.isContinuous());
        return c;
    }
}
