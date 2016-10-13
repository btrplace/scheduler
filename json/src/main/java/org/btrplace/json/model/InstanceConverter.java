/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.JSONObjectConverter;
import org.btrplace.json.model.constraint.ConstraintsConverter;
import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.OptConstraint;

import static org.btrplace.json.JSONs.checkKeys;

/**
 * A JSON converter for {@link org.btrplace.model.Instance}.
 *
 * @author Fabien Hermenier
 */
public class InstanceConverter implements JSONObjectConverter<Instance> {

    /**
     * Key that indicates the model.
     */
    private static final String MODEL_LABEL = "model";

    /**
     * Key that indicates the constraint list.
     */
    private static final String CONSTRAINTS_LABEL = "constraints";

    /**
     * Key that indicates the objective.
     */
    private static final String OBJ_LABEL = "objective";

    private ModelConverter moc;

    private ConstraintsConverter cc;

    public InstanceConverter() {
        moc = new ModelConverter();
        cc = ConstraintsConverter.newBundle();
    }
    @Override
    public Instance fromJSON(JSONObject in) throws JSONConverterException {
        checkKeys(in, MODEL_LABEL, CONSTRAINTS_LABEL, OBJ_LABEL);
        Model mo = moc.fromJSON((JSONObject) in.get(MODEL_LABEL));
        return new Instance(mo, cc.listFromJSON(mo, (JSONArray) in.get(CONSTRAINTS_LABEL)),
                (OptConstraint) cc.fromJSON(mo, (JSONObject) in.get(OBJ_LABEL)));
    }

    /**
     * Get the converter used to serialise models.
     * @return a converter
     */
    public ModelConverter getModelConverter() {
        return moc;
    }

    /**
     * Get the converter used to serialise constraints.
     * @return a converter
     */
    public ConstraintsConverter getConstraintsConverter() {
        return cc;
    }

    @Override
    public JSONObject toJSON(Instance instance) throws JSONConverterException {
        JSONObject ob = new JSONObject();
        ob.put(MODEL_LABEL, moc.toJSON(instance.getModel()));
        ob.put(CONSTRAINTS_LABEL, cc.toJSON(instance.getSatConstraints()));
        ob.put(OBJ_LABEL, cc.toJSON(instance.getOptConstraint()));
        return ob;
    }
}
