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
import org.btrplace.model.constraint.Ban;

/**
 * JSON converter for the {@link org.btrplace.model.constraint.Ban} constraint.
 *
 * @author Fabien Hermenier
 */
public class BanConverter extends ConstraintConverter<Ban> {

    @Override
    public Class<Ban> getSupportedConstraint() {
        return Ban.class;
    }

    @Override
    public String getJSONId() {
        return "ban";
    }

    @Override
    public Ban fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Ban(requiredVM(o, "vm"),
                requiredNodes(o, "nodes"),
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Ban o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", toJSON(o.getInvolvedVMs().iterator().next()));
        c.put("nodes", nodesToJSON(o.getInvolvedNodes()));
        c.put("continuous", o.isContinuous());
        return c;
    }
}
