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
import org.btrplace.model.constraint.NoDelay;

import static org.btrplace.json.JSONs.requiredVM;
/**
 * JSON Converter for the constraint {@link org.btrplace.model.constraint.NoDelay}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.model.constraint.NoDelay
 */
public class NoDelayConverter implements ConstraintConverter<NoDelay> {

    @Override
    public Class<NoDelay> getSupportedConstraint() {
        return NoDelay.class;
    }

    @Override
    public String getJSONId() {
        return "noDelay";
    }

    @Override
    public NoDelay fromJSON(Model mo, JSONObject in) throws JSONConverterException {
        checkId(in);
        return new NoDelay(requiredVM(mo, in, "vm"));
    }

    @Override
    public JSONObject toJSON(NoDelay noDelay) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", JSONs.elementToJSON(noDelay.getInvolvedVMs().iterator().next()));
        c.put("continuous", noDelay.isContinuous());
        return c;
    }
}
