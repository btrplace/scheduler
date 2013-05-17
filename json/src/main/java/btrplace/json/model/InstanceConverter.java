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
import btrplace.json.JSONConverterException;
import btrplace.json.model.constraint.SatConstraintsConverter;
import btrplace.model.Instance;
import btrplace.model.Model;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * A JSON converter for {@link btrplace.model.Instance}.
 *
 * @author Fabien Hermenier
 */

public class InstanceConverter implements JSONConverter<Instance> {

    @Override
    public Instance fromJSON(JSONObject in) throws JSONConverterException {
        ModelConverter moc = new ModelConverter();
        SatConstraintsConverter cstrc = new SatConstraintsConverter();

        Model mo = moc.fromJSON((JSONObject) in.get("model"));

        return new Instance(mo, cstrc.fromJSON((JSONArray) in.get("constraints")));
    }

    @Override
    public JSONObject toJSON(Instance instance) throws JSONConverterException {
        ModelConverter moc = new ModelConverter();
        SatConstraintsConverter cstrc = new SatConstraintsConverter();
        JSONObject ob = new JSONObject();
        ob.put("model", moc.toJSON(instance.getModel()));
        ob.put("constraints", cstrc.toJSON(instance.getConstraints()));
        return ob;
    }
}
