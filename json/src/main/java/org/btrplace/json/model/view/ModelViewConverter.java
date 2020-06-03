/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.view;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Model;
import org.btrplace.model.view.ModelView;

/**
 * Specify a JSON converter for a {@link org.btrplace.model.view.ModelView}.
 *
 * @author Fabien Hermenier
 */
public interface ModelViewConverter<E extends ModelView> {

    /**
     * The key identifier for the view type.
     */
    String IDENTIFIER = "id";

    /**
     * Get the className of the view that is supported by the converter.
     *
     * @return The view class
     */
    Class<E> getSupportedView();

    /**
     * Get the JSON identifier for the view.
     *
     * @return a non-empty string
     */
    String getJSONId();

    /**
     * Convert a json-encoded view.
     *
     * @param mo the model to rely on
     * @param o  the view to decode
     * @return the resulting view
     * @throws JSONConverterException if the conversion failed
     */
    E fromJSON(Model mo, JSONObject o) throws JSONConverterException;

    /**
     * Serialise a view.
     * @param o the view
     * @return the resulting encoded view
     * @throws JSONConverterException if a error occurred during the conversion
     */
    JSONObject toJSON(E o) throws JSONConverterException;

}
