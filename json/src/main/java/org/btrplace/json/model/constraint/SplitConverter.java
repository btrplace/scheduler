/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Split;

import java.util.Collection;

import static org.btrplace.json.JSONs.requiredBoolean;
import static org.btrplace.json.JSONs.requiredVMPart;
import static org.btrplace.json.JSONs.vmsToJSON;


/**
 * JSON converter for the {@link org.btrplace.model.constraint.Split} constraint.
 *
 * @author Fabien Hermenier
 */
public class SplitConverter implements ConstraintConverter<Split> {

    @Override
    public Class<Split> getSupportedConstraint() {
        return Split.class;
    }

    @Override
    public String getJSONId() {
        return "split";
    }

    @Override
    public Split fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Split(requiredVMPart(mo, o, "parts"), requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Split o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());

        JSONArray a = new JSONArray();
        for (Collection<VM> grp : o.getSets()) {
            a.add(vmsToJSON(grp));
        }

        c.put("parts", a);
        c.put("continuous", o.isContinuous());
        return c;
    }
}
