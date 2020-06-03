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
import org.btrplace.model.constraint.NoDelay;

import static org.btrplace.json.JSONs.requiredVM;
/**
 * JSON Converter for the constraint {@link org.btrplace.model.constraint.NoDelay}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.model.constraint.NoDelay
 */
public class NoDelayConverter implements ConstraintConverter<NoDelay> {

    @Override
    public Class<NoDelay> getSupportedConstraint() {
        return NoDelay.class;
    }

    @Override
    public String getJSONId() {
        return "noDelay";
    }

    @Override
    public NoDelay fromJSON(Model mo, JSONObject in) throws JSONConverterException {
        checkId(in);
        return new NoDelay(requiredVM(mo, in, "vm"));
    }

    @Override
    public JSONObject toJSON(NoDelay noDelay) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", JSONs.elementToJSON(noDelay.getInvolvedVMs().iterator().next()));
        c.put("continuous", noDelay.isContinuous());
        return c;
    }
}
