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
import org.btrplace.model.constraint.migration.MinMTTRMig;

/**
 * JSON Converter for the decommissioning objective {@link org.btrplace.model.constraint.migration.MinMTTRMig}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.model.constraint.migration.MinMTTRMig
 */
public class MinMTTRMigConverter implements ConstraintConverter<MinMTTRMig> {

    @Override
    public Class<MinMTTRMig> getSupportedConstraint() {
        return MinMTTRMig.class;
    }

    @Override
    public String getJSONId() {
        return "minimizeMTTRMigrationScheduling";
    }


    @Override
    public MinMTTRMig fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new MinMTTRMig();
    }

    @Override
    public JSONObject toJSON(MinMTTRMig o) {
        JSONObject c = new JSONObject();
        c.put("id", o.id());
        return c;
    }
}
