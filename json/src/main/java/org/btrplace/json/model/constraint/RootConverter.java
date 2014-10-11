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
import org.btrplace.model.constraint.Root;

/**
 * JSON converter for the {@link org.btrplace.model.constraint.Root} constraint.
 *
 * @author Fabien Hermenier
 */
public class RootConverter extends ConstraintConverter<Root> {


    @Override
    public Class<Root> getSupportedConstraint() {
        return Root.class;
    }

    @Override
    public String getJSONId() {
        return "root";
    }

    @Override
    public Root fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Root(requiredVM(o, "vm"));
    }

    @Override
    public JSONObject toJSON(Root o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("vm", toJSON(o.getInvolvedVMs().iterator().next()));
        return c;
    }
}
