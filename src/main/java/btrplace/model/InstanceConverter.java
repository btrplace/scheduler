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

package btrplace.model;

import btrplace.JSONConverter;
import btrplace.JSONConverterException;
import btrplace.model.constraint.SatConstraintsConverter;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A JSON converter for {@link Instance}.
 *
 * @author Fabien Hermenier
 */

public class InstanceConverter implements JSONConverter<Instance> {

    @Override
    public Instance fromJSON(JSONObject in) throws JSONConverterException {
        ModelConverter moc = new ModelConverter();
        SatConstraintsConverter cstrc = new SatConstraintsConverter();

        Model mo = moc.fromJSON((JSONObject) in.get("model"));

        List<SatConstraint> cstrs = new ArrayList<SatConstraint>();
        JSONArray arr = (JSONArray) in.get("constraints");
        for (Object o : arr) {
            cstrs.add(cstrc.fromJSON((JSONObject) o));
        }

        return new Instance(mo, cstrs);
    }

    @Override
    public JSONObject toJSON(Instance instance) throws JSONConverterException {
        ModelConverter moc = new ModelConverter();
        SatConstraintsConverter cstrc = new SatConstraintsConverter();
        JSONObject ob = new JSONObject();
        ob.put("model", moc.toJSON(instance.getModel()));
        JSONArray arr = new JSONArray();
        for (SatConstraint cstr : instance.getConstraints()) {
            arr.add(cstrc.toJSON(cstr));
        }
        ob.put("constraints", arr);
        return ob;
    }
}
