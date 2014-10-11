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

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.constraint.ResourceCapacity;

/**
 * JSON Converter for the constraint {@link ResourceCapacityConverter}.
 *
 * @author Fabien Hermenier
 */
public class ResourceCapacityConverter extends ConstraintConverter<ResourceCapacity> {

    @Override
    public Class<ResourceCapacity> getSupportedConstraint() {
        return ResourceCapacity.class;
    }

    @Override
    public String getJSONId() {
        return "resourceCapacity";
    }

    @Override
    public ResourceCapacity fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        return new ResourceCapacity(requiredNodes(o, "nodes"),
                requiredString(o, "rc"),
                requiredInt(o, "amount"),
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(ResourceCapacity o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("nodes", nodesToJSON(o.getInvolvedNodes()));
        c.put("rc", o.getResource());
        c.put("amount", o.getAmount());
        c.put("continuous", o.isContinuous());
        return c;
    }
}
