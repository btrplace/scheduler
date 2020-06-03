/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint.migration;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.constraint.ConstraintConverter;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.migration.Serialize;

import java.util.HashSet;

import static org.btrplace.json.JSONs.requiredVMs;
import static org.btrplace.json.JSONs.vmsToJSON;

/**
 * JSON Converter for the constraint {@link org.btrplace.model.constraint.migration.Serialize}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.model.constraint.migration.Serialize
 */
public class SerializeConverter implements ConstraintConverter<Serialize> {

    @Override
    public Class<Serialize> getSupportedConstraint() {
        return Serialize.class;
    }

    @Override
    public String getJSONId() {
        return "Serialize";
    }

    @Override
    public Serialize fromJSON(Model mo, JSONObject in) throws JSONConverterException {
        checkId(in);
        return new Serialize(new HashSet<>(requiredVMs(mo, in, "vms")));
    }

    @Override
    public JSONObject toJSON(Serialize serialize) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", vmsToJSON(serialize.getInvolvedVMs()));
        c.put("continuous", serialize.isContinuous());
        return c;
    }
}
