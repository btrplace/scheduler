/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Spread;

import java.util.HashSet;

import static org.btrplace.json.JSONs.requiredBoolean;
import static org.btrplace.json.JSONs.requiredVMs;
import static org.btrplace.json.JSONs.vmsToJSON;

/**
 * JSON converter for the {@link Spread} constraint.
 *
 * @author Fabien Hermenier
 */
public class SpreadConverter implements ConstraintConverter<Spread> {

    @Override
    public Class<Spread> getSupportedConstraint() {
        return Spread.class;
    }

    @Override
    public String getJSONId() {
        return "spread";
    }

    @Override
    public Spread fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Spread(new HashSet<>(requiredVMs(mo, o, "vms")), requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Spread o) {
        JSONObject c = new JSONObject();
        c.put(ConstraintConverter.IDENTIFIER, getJSONId());
        c.put("vms", vmsToJSON(o.getInvolvedVMs()));
        c.put("continuous", o.isContinuous());
        return c;
    }
}
