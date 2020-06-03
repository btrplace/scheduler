/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint.migration;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.JSONs;
import org.btrplace.json.model.constraint.ConstraintConverter;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.migration.Deadline;

import static org.btrplace.json.JSONs.requiredString;
import static org.btrplace.json.JSONs.requiredVM;

/**
 * JSON Converter for the constraint {@link org.btrplace.model.constraint.migration.Deadline}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.model.constraint.migration.Deadline
 */
public class DeadlineConverter implements ConstraintConverter<Deadline> {

    @Override
    public Class<Deadline> getSupportedConstraint() {
        return Deadline.class;
    }

    @Override
    public String getJSONId() {
        return "Deadline";
    }

    @Override
    public Deadline fromJSON(Model mo, JSONObject in) throws JSONConverterException {
        checkId(in);
        return new Deadline(requiredVM(mo, in, "vm"), requiredString(in, "timestamp"));
    }

    @Override
    public JSONObject toJSON(Deadline deadline) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", JSONs.elementToJSON(deadline.getInvolvedVMs().iterator().next()));
        c.put("timestamp", deadline.getTimestamp());
        c.put("continuous", deadline.isContinuous());
        return c;
    }
}
