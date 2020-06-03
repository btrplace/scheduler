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
import org.btrplace.model.constraint.Root;

import static org.btrplace.json.JSONs.requiredVM;
/**
 * JSON converter for the {@link org.btrplace.model.constraint.Root} constraint.
 *
 * @author Fabien Hermenier
 */
public class RootConverter implements ConstraintConverter<Root> {


    @Override
    public Class<Root> getSupportedConstraint() {
        return Root.class;
    }

    @Override
    public String getJSONId() {
        return "root";
    }

    @Override
    public Root fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Root(requiredVM(mo, o, "vm"));
    }

    @Override
    public JSONObject toJSON(Root o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", JSONs.elementToJSON(o.getInvolvedVMs().iterator().next()));
        return c;
    }
}
