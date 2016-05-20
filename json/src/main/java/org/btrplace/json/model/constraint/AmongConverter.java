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

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.constraint.Among;

import java.util.Collection;

import static org.btrplace.json.JSONs.*;

/**
 * JSON converter for the {@link Among} constraint.
 *
 * @author Fabien Hermenier
 */
public class AmongConverter implements ConstraintConverter<Among> {


    @Override
    public Class<Among> getSupportedConstraint() {
        return Among.class;
    }

    @Override
    public String getJSONId() {
        return "among";
    }

    @Override
    public Among fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);

        return new Among(requiredVMs(mo, o, "vms"),
                requiredNodePart(mo, o, "parts"),
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Among o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", vmsToJSON(o.getInvolvedVMs()));
        JSONArray a = new JSONArray();
        for (Collection<Node> grp : o.getGroupsOfNodes()) {
            a.add(nodesToJSON(grp));
        }
        c.put("parts", a);
        c.put("continuous", o.isContinuous());
        return c;
    }
}
