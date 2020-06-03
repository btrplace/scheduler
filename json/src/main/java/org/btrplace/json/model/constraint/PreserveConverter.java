/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Preserve;

import static org.btrplace.json.JSONs.elementToJSON;
import static org.btrplace.json.JSONs.requiredInt;
import static org.btrplace.json.JSONs.requiredString;
import static org.btrplace.json.JSONs.requiredVM;
/**
 * JSON Converter for the constraint {@link PreserveConverter}.
 *
 * @author Fabien Hermenier
 */
public class PreserveConverter implements ConstraintConverter<Preserve> {

    @Override
    public Class<Preserve> getSupportedConstraint() {
        return Preserve.class;
    }

    @Override
    public String getJSONId() {
        return "preserve";
    }

    @Override
    public Preserve fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Preserve(requiredVM(mo, o, "vm"),
                requiredString(o, "rc"),
                requiredInt(o, "amount"));
    }

    @Override
    public JSONObject toJSON(Preserve o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", elementToJSON(o.getInvolvedVMs().iterator().next()));
        c.put("rc", o.getResource());
        c.put("amount", o.getAmount());
        return c;
    }
}
