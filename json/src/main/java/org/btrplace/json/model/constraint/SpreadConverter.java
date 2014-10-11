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
import org.btrplace.model.constraint.Spread;

/**
 * JSON converter for the {@link Spread} constraint.
 *
 * @author Fabien Hermenier
 */
public class SpreadConverter extends ConstraintConverter<Spread> {

    @Override
    public Class<Spread> getSupportedConstraint() {
        return Spread.class;
    }

    @Override
    public String getJSONId() {
        return "spread";
    }

    @Override
    public Spread fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Spread(requiredVMs(o, "vms"), requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Spread o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vms", vmsToJSON(o.getInvolvedVMs()));
        c.put("continuous", o.isContinuous());
        return c;
    }
}
