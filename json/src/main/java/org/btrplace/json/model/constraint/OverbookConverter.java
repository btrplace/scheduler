/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Overbook;

import static org.btrplace.json.JSONs.elementToJSON;
import static org.btrplace.json.JSONs.requiredBoolean;
import static org.btrplace.json.JSONs.requiredDouble;
import static org.btrplace.json.JSONs.requiredNode;
import static org.btrplace.json.JSONs.requiredString;
/**
 * JSON Converter for the constraint {@link OverbookConverter}.
 *
 * @author Fabien Hermenier
 */
public class OverbookConverter implements ConstraintConverter<Overbook> {

    @Override
    public Class<Overbook> getSupportedConstraint() {
        return Overbook.class;
    }

    @Override
    public String getJSONId() {
        return "overbook";
    }

    @Override
    public Overbook fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Overbook(requiredNode(mo, o, "node"),
                requiredString(o, "rc"),
                requiredDouble(o, "ratio"),
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Overbook o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("node", elementToJSON(o.getInvolvedNodes().iterator().next()));
        c.put("rc", o.getResource());
        c.put("ratio", o.getRatio());
        c.put("continuous", o.isContinuous());
        return c;
    }
}
