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
import btrplace.model.constraint.CumulatedResourceCapacity;
import net.minidev.json.JSONObject;

/**
 * JSON Converter for the constraint {@link CumulatedResourceCapacityConverter}.
 *
 * @author Fabien Hermenier
 */
public class CumulatedResourceCapacityConverter extends SatConstraintConverter<CumulatedResourceCapacity> {

    @Override
    public Class<CumulatedResourceCapacity> getSupportedConstraint() {
        return CumulatedResourceCapacity.class;
    }

    @Override
    public String getJSONId() {
        return "cumulatedResourceCapacity";
    }

    @Override
    public CumulatedResourceCapacity fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        return new CumulatedResourceCapacity(requiredNodes(o, "nodes"),
                requiredString(o, "rcId"),
                requiredInt(o, "amount"),
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(CumulatedResourceCapacity o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("nodes", nodesToJSON(o.getInvolvedNodes()));
        c.put("rcId", o.getResource());
        c.put("amount", o.getAmount());
        c.put("continuous", o.isContinuous());
        return c;
    }
}
