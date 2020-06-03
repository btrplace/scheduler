/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.JSONs;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Running;

import static org.btrplace.json.JSONs.requiredVM;
/**
 * JSON Converter for the constraint {@link org.btrplace.model.constraint.Running}.
 *
 * @author Fabien Hermenier
 */
public class RunningConverter implements ConstraintConverter<Running> {


    @Override
    public Class<Running> getSupportedConstraint() {
        return Running.class;
    }

    @Override
    public String getJSONId() {
        return "running";
    }

    @Override
    public Running fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Running(requiredVM(mo, o, "vm"));
    }

    @Override
    public JSONObject toJSON(Running o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", JSONs.elementToJSON(o.getInvolvedVMs().iterator().next()));
        return c;
    }
}
