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

package btrplace.json.model;

import btrplace.json.AbstractJSONObjectConverter;
import btrplace.json.JSONConverterException;
import btrplace.json.model.constraint.ConstraintsConverter;
import btrplace.model.Instance;
import btrplace.model.Model;
import btrplace.model.constraint.OptConstraint;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * A JSON converter for {@link btrplace.model.Instance}.
 *
 * @author Fabien Hermenier
 */

public class InstanceConverter extends AbstractJSONObjectConverter<Instance> {

    @Override
    public Instance fromJSON(JSONObject in) throws JSONConverterException {
        ModelConverter moc = new ModelConverter();
        ConstraintsConverter cstrc = ConstraintsConverter.newBundle();

        Model mo = moc.fromJSON((JSONObject) in.get("model"));
        cstrc.setModel(mo);
        return new Instance(mo, cstrc.listFromJSON((JSONArray) in.get("constraints")),
                (OptConstraint) cstrc.fromJSON((JSONObject) in.get("objective")));
    }

    @Override
    public JSONObject toJSON(Instance instance) throws JSONConverterException {
        ModelConverter moc = new ModelConverter();
        ConstraintsConverter cstrc = ConstraintsConverter.newBundle();
        JSONObject ob = new JSONObject();
        ob.put("model", moc.toJSON(instance.getModel()));
        ob.put("constraints", cstrc.toJSON(instance.getSatConstraints()));
        ob.put("objective", cstrc.toJSON(instance.getOptConstraint()));
        return ob;
    }
}
