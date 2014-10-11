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

package org.btrplace.json.model;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.AbstractJSONObjectConverter;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.constraint.ConstraintsConverter;
import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.OptConstraint;

/**
 * A JSON converter for {@link org.btrplace.model.Instance}.
 *
 * @author Fabien Hermenier
 */

public class InstanceConverter extends AbstractJSONObjectConverter<Instance> {

    @Override
    public Instance fromJSON(JSONObject in) throws JSONConverterException {
        ModelConverter moc = new ModelConverter();
        ConstraintsConverter cConverter = ConstraintsConverter.newBundle();

        Model mo = moc.fromJSON((JSONObject) in.get("model"));
        cConverter.setModel(mo);
        return new Instance(mo, cConverter.listFromJSON((JSONArray) in.get("constraints")),
                (OptConstraint) cConverter.fromJSON((JSONObject) in.get("objective")));
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
