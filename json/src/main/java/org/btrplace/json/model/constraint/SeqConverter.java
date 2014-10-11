/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Seq;

import java.util.ArrayList;
import java.util.List;


/**
 * JSON converter for the {@link org.btrplace.model.constraint.Seq} constraint.
 *
 * @author Fabien Hermenier
 */
public class SeqConverter extends ConstraintConverter<Seq> {

    @Override
    public Class<Seq> getSupportedConstraint() {
        return Seq.class;
    }

    @Override
    public String getJSONId() {
        return "seq";
    }

    @Override
    public Seq fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        List<VM> s = new ArrayList<>();
        for (Object ob : (JSONArray) o.get("vms")) {
            s.add(getOrMakeVM((Integer) ob));
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
