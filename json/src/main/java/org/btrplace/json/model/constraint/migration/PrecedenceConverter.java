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
import org.btrplace.model.VM;
import org.btrplace.model.constraint.migration.Precedence;

import java.util.Iterator;

import static org.btrplace.json.JSONs.requiredVM;

/**
 * JSON Converter for the constraint {@link org.btrplace.model.constraint.migration.Precedence}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.model.constraint.migration.Precedence
 */
public class PrecedenceConverter implements ConstraintConverter<Precedence> {

    @Override
    public Class<Precedence> getSupportedConstraint() {
        return Precedence.class;
    }

    @Override
    public String getJSONId() {
        return "Precedence";
    }

    @Override
    public Precedence fromJSON(Model mo, JSONObject in) throws JSONConverterException {
        checkId(in);
        return new Precedence(requiredVM(mo, in, "vm1"), requiredVM(mo, in, "vm2"));
    }

    @Override
    public JSONObject toJSON(Precedence precedence) {
        JSONObject c = new JSONObject();
        Iterator<VM> it = precedence.getInvolvedVMs().iterator();
        c.put("id", getJSONId());
        c.put("vm1", JSONs.elementToJSON(it.next()));
        c.put("vm2", JSONs.elementToJSON(it.next()));
        c.put("continuous", precedence.isContinuous());
        return c;
    }
}
