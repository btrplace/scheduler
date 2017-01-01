/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

package org.btrplace.safeplace.testing.verification.btrplace;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.JSONs;
import org.btrplace.json.model.constraint.ConstraintConverter;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Spread;

/**
 * JSON converter for the {@link Spread} constraint.
 *
 * @author Fabien Hermenier
 */
public class ScheduleConverter implements ConstraintConverter<Schedule> {

    @Override
    public Class<Schedule> getSupportedConstraint() {
        return Schedule.class;
    }

    @Override
    public String getJSONId() {
        return "schedule";
    }

    @Override
    public Schedule fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        if (o.containsKey("node")) {
            return new Schedule(JSONs.requiredNode(mo, o, "node"), JSONs.requiredInt(o, "start"), JSONs.requiredInt(o, "END"));
        }
        return new Schedule(JSONs.requiredVM(mo, o, "vm"), JSONs.requiredInt(o, "start"), JSONs.requiredInt(o, "END"));
    }

    @Override
    public JSONObject toJSON(Schedule o) {

        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        if (o.getNode() != null) {
            c.put("node", JSONs.elementToJSON(o.getNode()));
        } else {
            c.put("vm", JSONs.elementToJSON(o.getVM()));
        }
        c.put("start",o.getStart());
        c.put("END", o.getEnd());
        return c;
    }
}
