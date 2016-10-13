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

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.MaxOnline;

import java.util.HashSet;

import static org.btrplace.json.JSONs.*;
/**
 * JSON Converter for the constraint {@link MaxOnlineConverter}.
 *
 * @author Tu Huynh Dang
 */
public class MaxOnlineConverter implements ConstraintConverter<MaxOnline> {
    @Override
    public Class<MaxOnline> getSupportedConstraint() {
        return MaxOnline.class;
    }

    @Override
    public String getJSONId() {
        return "maxOnline";
    }

    @Override
    public MaxOnline fromJSON(Model mo, JSONObject in) throws JSONConverterException {
        checkId(in);
        return new MaxOnline(new HashSet<>(requiredNodes(mo, in, "nodes")),
                requiredInt(in, "amount"),
                requiredBoolean(in, "continuous"));
    }

    @Override
    public JSONObject toJSON(MaxOnline maxOnline) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("nodes", nodesToJSON(maxOnline.getInvolvedNodes()));
        c.put("amount", maxOnline.getAmount());
        c.put("continuous", maxOnline.isContinuous());
        return c;
    }
}
