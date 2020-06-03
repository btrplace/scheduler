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
import org.btrplace.model.constraint.Offline;

import static org.btrplace.json.JSONs.requiredNode;
/**
 * JSON Converter for the constraint {@link org.btrplace.model.constraint.Online}.
 *
 * @author Fabien Hermenier
 */
public class OfflineConverter implements ConstraintConverter<Offline> {


    @Override
    public Class<Offline> getSupportedConstraint() {
        return Offline.class;
    }

    @Override
    public String getJSONId() {
        return "offline";
    }


    @Override
    public Offline fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Offline(requiredNode(mo, o, "node"));
    }

    @Override
    public JSONObject toJSON(Offline o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("node", JSONs.elementToJSON(o.getInvolvedNodes().iterator().next()));
        return c;
    }
}
