/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Gather;

import static org.btrplace.json.JSONs.requiredBoolean;
import static org.btrplace.json.JSONs.requiredVMs;
import static org.btrplace.json.JSONs.vmsToJSON;
/**
 * JSON converter for the {@link Gather} constraint.
 *
 * @author Fabien Hermenier
 */
public class GatherConverter implements ConstraintConverter<Gather> {

    @Override
    public Class<Gather> getSupportedConstraint() {
        return Gather.class;
    }

    @Override
    public String getJSONId() {
        return "gather";
    }


    @Override
    public Gather fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Gather(requiredVMs(mo, o, "vms"), requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Gather o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", vmsToJSON(o.getInvolvedVMs()));
        c.put("continuous", o.isContinuous());
        return c;
    }
}
