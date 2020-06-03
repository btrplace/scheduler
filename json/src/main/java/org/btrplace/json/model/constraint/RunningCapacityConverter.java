/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.RunningCapacity;

import java.util.HashSet;

import static org.btrplace.json.JSONs.nodesToJSON;
import static org.btrplace.json.JSONs.requiredBoolean;
import static org.btrplace.json.JSONs.requiredInt;
import static org.btrplace.json.JSONs.requiredNodes;

/**
 * JSON Converter for the constraint {@link RunningCapacity}.
 *
 * @author Fabien Hermenier
 */
public class RunningCapacityConverter implements ConstraintConverter<RunningCapacity> {

    @Override
    public Class<RunningCapacity> getSupportedConstraint() {
        return RunningCapacity.class;
    }

    @Override
    public String getJSONId() {
        return "runningCapacity";
    }


    @Override
    public RunningCapacity fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new RunningCapacity(new HashSet<>(requiredNodes(mo, o, "nodes")),
                requiredInt(o, "amount"),
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(RunningCapacity o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("nodes", nodesToJSON(o.getInvolvedNodes()));
        c.put("amount", o.getAmount());
        c.put("continuous", o.isContinuous());
        return c;
    }
}
