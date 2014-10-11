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

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Split;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * JSON converter for the {@link org.btrplace.model.constraint.Split} constraint.
 *
 * @author Fabien Hermenier
 */
public class SplitConverter extends ConstraintConverter<Split> {

    @Override
    public Class<Split> getSupportedConstraint() {
        return Split.class;
    }

    @Override
    public String getJSONId() {
        return "split";
    }

    @Override
    public Split fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        Set<Collection<VM>> vms = new HashSet<>();
        Object x = o.get("parts");
        if (!(x instanceof JSONArray)) {
            throw new JSONConverterException("Set of identifiers sets expected at key 'parts'");
        }
        for (Object obj : (JSONArray) x) {
            vms.add(vmsFromJSON((JSONArray) obj));
        }

        return new Split(vms, requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Split o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());

        JSONArray a = new JSONArray();
        for (Collection<VM> grp : o.getSets()) {
            a.add(vmsToJSON(grp));
        }

        c.put("parts", a);
        c.put("continuous", o.isContinuous());
        return c;
    }
}
