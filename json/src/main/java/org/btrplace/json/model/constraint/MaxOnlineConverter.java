/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.MaxOnline;

import java.util.HashSet;

import static org.btrplace.json.JSONs.nodesToJSON;
import static org.btrplace.json.JSONs.requiredBoolean;
import static org.btrplace.json.JSONs.requiredInt;
import static org.btrplace.json.JSONs.requiredNodes;
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
