/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.model.view;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.view.network.NetworkConverter;
import org.btrplace.model.Model;
import org.btrplace.model.view.ModelView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.btrplace.json.JSONs.checkKeys;

/**
 * Extensible converter for {@link org.btrplace.model.view.ModelView}.
 *
 * @author Fabien Hermenier
 */
public class ModelViewsConverter {

  private final Map<Class<? extends ModelView>, ModelViewConverter<? extends ModelView>> java2json;
  private final Map<String, ModelViewConverter<? extends ModelView>> json2java;

  /**
   * Make a new empty converter.
   */
  public ModelViewsConverter() {
    java2json = new HashMap<>();
    json2java = new HashMap<>();
  }

  /**
     * Make a new {@code ModelViewsConverter} and fulfill it using converters for the following decorators:
     * <ul>
     * <li>{@link org.btrplace.json.model.view.ShareableResourceConverter}</li>
     * <li>{@link org.btrplace.json.model.view.NamingServiceConverter}</li>
     * <li>{@link NetworkConverter}</li>
     * </ul>
     *
     * @return a fulfilled converter.
     */
    public static ModelViewsConverter newBundle() {
        ModelViewsConverter converter = new ModelViewsConverter();
        converter.register(new ShareableResourceConverter());
        converter.register(new NamingServiceConverter());
        converter.register(new NetworkConverter());
        return converter;
    }

    /**
     * Register a converter for a specific view.
     *
     * @param c the converter to register
     */
    public void register(ModelViewConverter<? extends ModelView> c) {
        java2json.put(c.getSupportedView(), c);
        json2java.put(c.getJSONId(), c);

    }

    /**
     * Get the Java view that are supported by the converter.
     *
     * @return a set of classes derived from {@link org.btrplace.model.view.ModelView} that may be empty
     */
    public Set<Class<? extends ModelView>> getSupportedJavaViews() {
        return java2json.keySet();
    }

    /**
     * Get the JSON decorators that are supported by the converter.
     *
     * @return a set of view identifiers that may be empty
     */
    public Set<String> getSupportedJSONViews() {
        return json2java.keySet();
    }

    /**
     * Convert a json-encoded view.
     *
     * @param mo the model to rely on
     * @param in the view to decode
     * @return the resulting view
     * @throws JSONConverterException if the conversion failed
     */
    public ModelView fromJSON(Model mo, JSONObject in) throws JSONConverterException {
        checkKeys(in, ModelViewConverter.IDENTIFIER);
        Object id = in.get(ModelViewConverter.IDENTIFIER);
        ModelViewConverter<? extends ModelView> c = json2java.get(id.toString());
        if (c == null) {
            throw new JSONConverterException("No converter available for a view having id '" + id + "'");
        }
        return c.fromJSON(mo, in);
    }

    /**
     * Serialise a view.
     * @param o the view
     * @return the resulting encoded view
     * @throws JSONConverterException if a error occurred during the conversion
     */
    public JSONObject toJSON(ModelView o) throws JSONConverterException {
        ModelViewConverter c = java2json.get(o.getClass());
        if (c == null) {
            throw new JSONConverterException("No converter available for a view with the '" + o.getClass() + "' className");
        }
        return c.toJSON(o);
    }
}
