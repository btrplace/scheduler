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
import org.btrplace.model.constraint.Ban;

import static org.btrplace.json.JSONs.*;

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
    public Ban fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Ban(requiredVM(mo, o, "vm"),
                requiredNodes(mo, o, "nodes"),
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Ban o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", JSONs.toJSON(o.getInvolvedVMs().iterator().next()));
        c.put("nodes", nodesToJSON(o.getInvolvedNodes()));
        c.put("continuous", o.isContinuous());
        return c;
    }
}
