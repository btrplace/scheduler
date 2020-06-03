/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
