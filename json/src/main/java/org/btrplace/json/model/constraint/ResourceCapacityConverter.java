/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.ResourceCapacity;

import java.util.HashSet;

import static org.btrplace.json.JSONs.nodesToJSON;
import static org.btrplace.json.JSONs.requiredBoolean;
import static org.btrplace.json.JSONs.requiredInt;
import static org.btrplace.json.JSONs.requiredNodes;
import static org.btrplace.json.JSONs.requiredString;
/**
 * JSON Converter for the constraint {@link ResourceCapacityConverter}.
 *
 * @author Fabien Hermenier
 */
public class ResourceCapacityConverter implements ConstraintConverter<ResourceCapacity> {

    @Override
    public Class<ResourceCapacity> getSupportedConstraint() {
        return ResourceCapacity.class;
    }

    @Override
    public String getJSONId() {
        return "resourceCapacity";
    }

    @Override
    public ResourceCapacity fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new ResourceCapacity(new HashSet<>(requiredNodes(mo, o, "nodes")),
                requiredString(o, "rc"),
                requiredInt(o, "amount"),
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(ResourceCapacity o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("nodes", nodesToJSON(o.getInvolvedNodes()));
        c.put("rc", o.getResource());
        c.put("amount", o.getAmount());
        c.put("continuous", o.isContinuous());
        return c;
    }
}
