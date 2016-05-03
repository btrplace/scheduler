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

package org.btrplace.json.model.view.network;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.view.network.Routing;

/**
 * Specify a JSON converter for a {@link Constraint}.
 *
 * @author Fabien Hermenier
 */
public interface RoutingConverter<E extends Routing> {

    /**
     * Get the name of the constraint that is supported by the converter.
     *
     * @return The constraint class
     */
    Class<E> getSupportedRouting();

    /**
     * Get the JSON identifier for the constraint.
     *
     * @return a non-empty string
     */
    String getJSONId();

    /**
     * Check if the JSON object can be converted using this converter.
     * For being convertible, the key 'id' must be equals to {@link #getJSONId()}.
     *
     * @param o the object to test
     * @throws JSONConverterException if the object is not compatible
     */
    default void checkId(JSONObject o) throws JSONConverterException {
        Object id = o.get("id");
        if (id == null || !id.toString().equals(getJSONId())) {
            throw new JSONConverterException("Incorrect converter for " + o.toJSONString() + ". Expecting a routing id '" + id + "'");
        }
    }

    /**
     * Decode a routing.
     *
     * @param mo the model to rely on
     * @param o  the routing to decode
     * @return the conversion result
     * @throws JSONConverterException if an error occurred while decoding the routing
     */
    E fromJSON(Model mo, JSONObject o) throws JSONConverterException;

    /**
     * Serialize a routing.
     *
     * @param o the routing to serialize
     * @return the conversion result
     */
    JSONObject toJSON(E o);

}
