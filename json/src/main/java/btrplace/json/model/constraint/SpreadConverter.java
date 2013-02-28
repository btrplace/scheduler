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
import btrplace.json.Utils;
import btrplace.model.constraint.Spread;
import net.minidev.json.JSONObject;

/**
 * JSON converter for the {@link Spread} constraint.
 *
 * @author Fabien Hermenier
 */
public class SpreadConverter extends SatConstraintConverter<Spread> {

    @Override
    public Class<Spread> getSupportedConstraint() {
        return Spread.class;
    }

    @Override
    public String getJSONId() {
        return "spread";
    }

    @Override
    public Spread fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Spread(Utils.requiredUUIDs(o, "vms"), Utils.requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Spread o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", Utils.toJSON(o.getInvolvedVMs()));
        c.put("continuous", o.isContinuous());
        return c;
    }
}
