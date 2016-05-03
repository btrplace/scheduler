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
import org.btrplace.json.AbstractJSONObjectConverter;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Ready;

import static org.btrplace.json.AbstractJSONObjectConverter.requiredBoolean;
import static org.btrplace.json.AbstractJSONObjectConverter.requiredVM;
/**
 * JSON Converter for the constraint {@link Ready}.
 *
 * @author Fabien Hermenier
 */
public class ReadyConverter extends ConstraintConverter<Ready> {


    @Override
    public Class<Ready> getSupportedConstraint() {
        return Ready.class;
    }

    @Override
    public String getJSONId() {
        return "ready";
    }

    @Override
    public Ready fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Ready(requiredVM(mo, o, "vm"), requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Ready o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", AbstractJSONObjectConverter.toJSON(o.getInvolvedVMs().iterator().next()));
        c.put("continuous", o.isContinuous());
        return c;
    }
}
