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


import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.constraint.Killed;

/**
 * JSON Converter for the constraint {@link org.btrplace.model.constraint.Killed}.
 *
 * @author Fabien Hermenier
 */
public class KilledConverter extends ConstraintConverter<Killed> {

    @Override
    public Class<Killed> getSupportedConstraint() {
        return Killed.class;
    }

    @Override
    public String getJSONId() {
        return "killed";
    }


    @Override
    public Killed fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Killed(requiredVM(o, "vm"), requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Killed o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", toJSON(o.getInvolvedVMs().iterator().next()));
        c.put("continuous", o.isContinuous());
        return c;
    }
}
