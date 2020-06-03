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
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Seq;

import java.util.ArrayList;
import java.util.List;

import static org.btrplace.json.JSONs.getVM;
import static org.btrplace.json.JSONs.vmsToJSON;

/**
 * JSON converter for the {@link org.btrplace.model.constraint.Seq} constraint.
 *
 * @author Fabien Hermenier
 */
public class SeqConverter implements ConstraintConverter<Seq> {

    @Override
    public Class<Seq> getSupportedConstraint() {
        return Seq.class;
    }

    @Override
    public String getJSONId() {
        return "seq";
    }

    @Override
    public Seq fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        List<VM> s = new ArrayList<>();
        for (Object ob : (JSONArray) o.get("vms")) {
            s.add(getVM(mo, (Integer) ob));
        }
        return new Seq(s);
    }

    @Override
    public JSONObject toJSON(Seq o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", vmsToJSON(o.getInvolvedVMs()));
        return c;
    }
}
