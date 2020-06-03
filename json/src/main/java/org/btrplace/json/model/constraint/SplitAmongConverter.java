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
import org.btrplace.model.VM;
import org.btrplace.model.constraint.SplitAmong;

import java.util.Collection;

import static org.btrplace.json.JSONs.nodesToJSON;
import static org.btrplace.json.JSONs.requiredBoolean;
import static org.btrplace.json.JSONs.requiredNodePart;
import static org.btrplace.json.JSONs.requiredVMPart;
import static org.btrplace.json.JSONs.vmsToJSON;

/**
 * JSON converter for the {@link SplitAmong} constraint.
 *
 * @author Fabien Hermenier
 */
public class SplitAmongConverter implements ConstraintConverter<SplitAmong> {

    @Override
    public Class<SplitAmong> getSupportedConstraint() {
        return SplitAmong.class;
    }

    @Override
    public String getJSONId() {
        return "splitAmong";
    }

    @Override
    public SplitAmong fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new SplitAmong(requiredVMPart(mo, o, "vParts"), requiredNodePart(mo, o, "pParts"), requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(SplitAmong o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());

        JSONArray vGroups = new JSONArray();
        for (Collection<VM> grp : o.getGroupsOfVMs()) {
            vGroups.add(vmsToJSON(grp));
        }

        JSONArray pGroups = new JSONArray();
        for (Collection<Node> grp : o.getGroupsOfNodes()) {
            pGroups.add(nodesToJSON(grp));
        }

        c.put("vParts", vGroups);
        c.put("pParts", pGroups);
        c.put("continuous", o.isContinuous());
        return c;
    }
}
