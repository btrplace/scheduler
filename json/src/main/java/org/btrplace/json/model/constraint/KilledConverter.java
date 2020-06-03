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
import org.btrplace.model.constraint.Killed;

import static org.btrplace.json.JSONs.requiredVM;
/**
 * JSON Converter for the constraint {@link org.btrplace.model.constraint.Killed}.
 *
 * @author Fabien Hermenier
 */
public class KilledConverter implements ConstraintConverter<Killed> {

    @Override
    public Class<Killed> getSupportedConstraint() {
        return Killed.class;
    }

    @Override
    public String getJSONId() {
        return "killed";
    }


    @Override
    public Killed fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Killed(requiredVM(mo, o, "vm"));
    }

    @Override
    public JSONObject toJSON(Killed o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", JSONs.elementToJSON(o.getInvolvedVMs().iterator().next()));
        return c;
    }
}
