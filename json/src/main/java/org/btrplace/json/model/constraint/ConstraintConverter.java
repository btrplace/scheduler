/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Constraint;

/**
 * Specify a JSON converter for a {@link org.btrplace.model.constraint.Constraint}.
 *
 * @author Fabien Hermenier
 */
public interface ConstraintConverter<E extends Constraint> {

  /**
   * The key identifier for the view type.
   */
  String IDENTIFIER = "id";

    /**
     * Get the name of the constraint that is supported by the converter.
     *
     * @return The constraint class
     */
    Class<E> getSupportedConstraint();

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
      Object id = o.get(ConstraintConverter.IDENTIFIER);
        if (id == null || !id.toString().equals(getJSONId())) {
            throw new JSONConverterException("Incorrect converter for " + o.toJSONString() + ". Expecting a constraint id '" + id + "'");
        }
    }

    /**
     * Convert a json-encoded constraint.
     *
     * @param mo the model to rely on
     * @param o  the constraint to decode
     * @return the resulting constraint
     * @throws JSONConverterException if the conversion failed
     */
    E fromJSON(Model mo, JSONObject o) throws JSONConverterException;

    /**
     * Serialise a constraint.
     * @param o the constraint
     * @return the resulting encoded constraint
     */
    JSONObject toJSON(E o);

    /**
     * Serialise a constraint.
     * @param d the constraint to serialise
     * @return the JSON-encoded constraint
     */
    default String toJSONString(E d) {
        return toJSON(d).toJSONString();
    }
}
