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
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Overbook;

import static org.btrplace.json.JSONs.*;
/**
 * JSON Converter for the constraint {@link OverbookConverter}.
 *
 * @author Fabien Hermenier
 */
public class OverbookConverter implements ConstraintConverter<Overbook> {

    @Override
    public Class<Overbook> getSupportedConstraint() {
        return Overbook.class;
    }

    @Override
    public String getJSONId() {
        return "overbook";
    }

    @Override
    public Overbook fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Overbook(requiredNode(mo, o, "node"),
                requiredString(o, "rc"),
                requiredDouble(o, "ratio"),
                requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Overbook o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("node", elementToJSON(o.getInvolvedNodes().iterator().next()));
        c.put("rc", o.getResource());
        c.put("ratio", o.getRatio());
        c.put("continuous", o.isContinuous());
        return c;
    }
}
