/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Ban;

import static org.btrplace.json.JSONs.elementToJSON;
import static org.btrplace.json.JSONs.nodesToJSON;
import static org.btrplace.json.JSONs.requiredBoolean;
import static org.btrplace.json.JSONs.requiredNodes;
import static org.btrplace.json.JSONs.requiredVM;

/**
 * JSON converter for the {@link org.btrplace.model.constraint.Ban} constraint.
 *
 * @author Fabien Hermenier
 */
public class BanConverter implements ConstraintConverter<Ban> {

    @Override
    public Class<Ban> getSupportedConstraint() {
        return Ban.class;
    }

    @Override
    public String getJSONId() {
        return "ban";
    }

    @Override
    public Ban fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Ban(requiredVM(mo, o, "vm"),
                requiredNodes(mo, o, "nodes"),
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Ban o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", elementToJSON(o.getInvolvedVMs().iterator().next()));
        c.put("nodes", nodesToJSON(o.getInvolvedNodes()));
        c.put("continuous", o.isContinuous());
        return c;
    }
}
