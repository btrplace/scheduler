/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.model.constraint;

import btrplace.JSONConverter;
import btrplace.JSONConverterException;
import btrplace.model.SatConstraint;
import net.minidev.json.JSONObject;

/**
 * Specify a JSON converter for a {@link btrplace.model.SatConstraint}.
 *
 * @author Fabien Hermenier
 */
public abstract class SatConstraintConverter<E extends SatConstraint> implements JSONConverter<E> {

    /**
     * Get the name of the constraint that is supported by the converter.
     *
     * @return The constraint class
     */
    public abstract Class<E> getSupportedConstraint();

    /**
     * Get the JSON identifier for the constraint.
     *
     * @return a non-empty string
     */
    public abstract String getJSONId();

    public void checkId(JSONObject o) throws JSONConverterException {
        Object id = o.get("id");
        if (id != null && !id.toString().equals(getJSONId())) {
            throw new JSONConverterException("Incorrect converter for " + o.toJSONString() + ". Expecting a constraint id '" + id + "'");
        }

    }
}
