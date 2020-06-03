/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.MinMigrations;

/**
 * Converter for Opt-constraint {@link MinMigrations}.
 *
 * @author Fabien Hermenier
 */
public class MinMigrationsConverter implements ConstraintConverter<MinMigrations> {

    @Override
    public Class<MinMigrations> getSupportedConstraint() {
        return MinMigrations.class;
    }

    @Override
    public String getJSONId() {
        return "minimizeMigrations";
    }


    @Override
    public MinMigrations fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new MinMigrations();
    }

    @Override
    public JSONObject toJSON(MinMigrations o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        return c;
    }
}
