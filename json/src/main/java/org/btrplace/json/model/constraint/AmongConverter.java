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
import org.btrplace.model.Node;
import org.btrplace.model.constraint.Among;

import java.util.Collection;

import static org.btrplace.json.JSONs.nodesToJSON;
import static org.btrplace.json.JSONs.requiredBoolean;
import static org.btrplace.json.JSONs.requiredNodePart;
import static org.btrplace.json.JSONs.requiredVMs;
import static org.btrplace.json.JSONs.vmsToJSON;

/**
 * JSON converter for the {@link Among} constraint.
 *
 * @author Fabien Hermenier
 */
public class AmongConverter implements ConstraintConverter<Among> {


    @Override
    public Class<Among> getSupportedConstraint() {
        return Among.class;
    }

    @Override
    public String getJSONId() {
        return "among";
    }

    @Override
    public Among fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);

        return new Among(requiredVMs(mo, o, "vms"),
                requiredNodePart(mo, o, "parts"),
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Among o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", vmsToJSON(o.getInvolvedVMs()));
        JSONArray a = new JSONArray();
        for (Collection<Node> grp : o.getGroupsOfNodes()) {
            a.add(nodesToJSON(grp));
        }
        c.put("parts", a);
        c.put("continuous", o.isContinuous());
        return c;
    }
}
