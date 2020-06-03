/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.MinMTTR;

/**
 * @author Fabien Hermenier
 */
public class MinMTTRConverter implements ConstraintConverter<MinMTTR> {

    @Override
    public Class<MinMTTR> getSupportedConstraint() {
        return MinMTTR.class;
    }

    @Override
    public String getJSONId() {
        return "minimizeMTTR";
    }


    @Override
    public MinMTTR fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new MinMTTR();
    }

    @Override
    public JSONObject toJSON(MinMTTR o) {
        JSONObject c = new JSONObject();
        c.put("id", o.id());
        return c;
    }
}
