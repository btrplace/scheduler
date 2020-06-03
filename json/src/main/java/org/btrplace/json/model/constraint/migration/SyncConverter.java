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
import org.btrplace.model.constraint.migration.Sync;

import static org.btrplace.json.JSONs.requiredVMs;
import static org.btrplace.json.JSONs.vmsToJSON;

/**
 * JSON Converter for the constraint {@link org.btrplace.model.constraint.migration.Sync}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.model.constraint.migration.Sync
 */
public class SyncConverter implements ConstraintConverter<Sync> {

    @Override
    public Class<Sync> getSupportedConstraint() {
        return Sync.class;
    }

    @Override
    public String getJSONId() {
        return "Sync";
    }

    @Override
    public Sync fromJSON(Model mo, JSONObject in) throws JSONConverterException {
        checkId(in);
        return new Sync(requiredVMs(mo, in, "vms"));
    }

    @Override
    public JSONObject toJSON(Sync sync) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", vmsToJSON(sync.getInvolvedVMs()));
        c.put("continuous", sync.isContinuous());
        return c;
    }
}
