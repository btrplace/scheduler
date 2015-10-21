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

package org.btrplace.json.model.constraint.migration;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.constraint.ConstraintConverter;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.migration.Precedence;

import java.util.Iterator;

/**
 * JSON Converter for the constraint {@link org.btrplace.model.constraint.migration.Precedence}.
 *
 * @author Vincent Kherbache
 * @see org.btrplace.model.constraint.migration.Precedence
 */
public class PrecedenceConverter extends ConstraintConverter<Precedence> {

    @Override
    public Class<Precedence> getSupportedConstraint() {
        return Precedence.class;
    }

    @Override
    public String getJSONId() {
        return "Precedence";
    }

    @Override
    public Precedence fromJSON(JSONObject in) throws JSONConverterException {
        checkId(in);
        return new Precedence(requiredVM(in, "vm1"), requiredVM(in, "vm2"));
    }

    @Override
    public JSONObject toJSON(Precedence precedence) throws JSONConverterException {
        JSONObject c = new JSONObject();
        Iterator<VM> it = precedence.getInvolvedVMs().iterator();
        c.put("id", getJSONId());
        c.put("vm1", toJSON(it.next()));
        c.put("vm2", toJSON(it.next()));
        c.put("continuous", precedence.isContinuous());
        return c;
    }
}
