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

/**
 * JSON Converter for the constraint {@link OverbookConverter}.
 *
 * @author Fabien Hermenier
 */
public class OverbookConverter implements SatConstraintConverter<Overbook> {

    @Override
    public Class<Overbook> getSupportedConstraint() {
        return Overbook.class;
    }

    @Override
    public String getJSONId() {
        return "overbook";
    }

    @Override
    public Overbook fromJSON(JSONObject o) {
        String id = o.get("id").toString();
        if (!id.equals(getJSONId())) {
            return null;
        }
        return new Overbook(Utils.fromJSON((JSONArray) o.get("vms")),
                (String) o.get("rcId"),
                (Double) o.get("ratio"),
                (Boolean) o.get("continuous"));
    }

    @Override
    public JSONObject toJSON(Overbook o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", Utils.toJSON(o.getInvolvedNodes()));
        c.put("rcId", o.getResource());
        c.put("ratio", o.getRatio());
        c.put("continuous", o.isContinuous());
        return c;
    }
}
