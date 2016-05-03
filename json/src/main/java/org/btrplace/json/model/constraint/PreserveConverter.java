/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.JSONs;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Preserve;

import static org.btrplace.json.JSONs.*;
/**
 * JSON Converter for the constraint {@link PreserveConverter}.
 *
 * @author Fabien Hermenier
 */
public class PreserveConverter implements ConstraintConverter<Preserve> {

    @Override
    public Class<Preserve> getSupportedConstraint() {
        return Preserve.class;
    }

    @Override
    public String getJSONId() {
        return "preserve";
    }

    @Override
    public Preserve fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Preserve(requiredVM(mo, o, "vm"),
                requiredString(o, "rc"),
                requiredInt(o, "amount"));
    }

    @Override
    public JSONObject toJSON(Preserve o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", JSONs.toJSON(o.getInvolvedVMs().iterator().next()));
        c.put("rc", o.getResource());
        c.put("amount", o.getAmount());
        return c;
    }
}
