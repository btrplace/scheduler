/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
