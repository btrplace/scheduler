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
import org.btrplace.model.constraint.Online;

/**
 * JSON Converter for the constraint {@link Online}.
 *
 * @author Fabien Hermenier
 */
public class OnlineConverter extends ConstraintConverter<Online> {


    @Override
    public Class<Online> getSupportedConstraint() {
        return Online.class;
    }

    @Override
    public String getJSONId() {
        return "online";
    }


    @Override
    public Online fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Online(requiredNode(o, "node"), requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Online o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("node", toJSON(o.getInvolvedNodes().iterator().next()));
        c.put("continuous", o.isContinuous());
        return c;
    }
}
