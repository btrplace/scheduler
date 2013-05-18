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
import btrplace.model.constraint.Ban;
import net.minidev.json.JSONObject;

/**
 * JSON converter for the {@link btrplace.model.constraint.Ban} constraint.
 *
 * @author Fabien Hermenier
 */
public class BanConverter extends SatConstraintConverter<Ban> {

    @Override
    public Class<Ban> getSupportedConstraint() {
        return Ban.class;
    }

    @Override
    public String getJSONId() {
        return "ban";
    }

    @Override
    public Ban fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Ban(requiredUUIDs(o, "vms"),
                requiredUUIDs(o, "nodes"));
    }

    @Override
    public JSONObject toJSON(Ban o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", toJSON(o.getInvolvedVMs()));
        c.put("nodes", toJSON(o.getInvolvedNodes()));
        return c;
    }
}
