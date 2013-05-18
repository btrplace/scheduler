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
import btrplace.model.constraint.SequentialVMTransitions;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JSON converter for the {@link btrplace.model.constraint.SequentialVMTransitions} constraint.
 *
 * @author Fabien Hermenier
 */
public class SequentialVMTransitionsConverter extends SatConstraintConverter<SequentialVMTransitions> {

    @Override
    public Class<SequentialVMTransitions> getSupportedConstraint() {
        return SequentialVMTransitions.class;
    }

    @Override
    public String getJSONId() {
        return "sequentialVMTransitions";
    }

    @Override
    public SequentialVMTransitions fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        List<UUID> s = new ArrayList<>();
        for (Object ob : (JSONArray) o.get("vms")) {
            s.add(UUID.fromString((String) ob));
        }
        return new SequentialVMTransitions(s);
    }

    @Override
    public JSONObject toJSON(SequentialVMTransitions o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", toJSON(o.getInvolvedVMs()));
        return c;
    }
}
