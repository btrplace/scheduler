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
import btrplace.model.constraint.SingleRunningCapacity;
import net.minidev.json.JSONObject;


/**
 * JSON Converter for the constraint {@link SingleRunningCapacityConverter}.
 *
 * @author Fabien Hermenier
 */
public class SingleRunningCapacityConverter extends SatConstraintConverter<SingleRunningCapacity> {

    @Override
    public Class<SingleRunningCapacity> getSupportedConstraint() {
        return SingleRunningCapacity.class;
    }

    @Override
    public String getJSONId() {
        return "singleRunningCapacity";
    }

    @Override
    public SingleRunningCapacity fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        return new SingleRunningCapacity(requiredUUIDs(o, "nodes"),
                (int) requiredLong(o, "amount"),
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(SingleRunningCapacity o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("nodes", uuidsToJSON(o.getInvolvedNodes()));
        c.put("amount", (long) o.getAmount());
        c.put("continuous", o.isContinuous());
        return c;
    }
}
