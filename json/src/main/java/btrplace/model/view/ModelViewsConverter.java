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

package btrplace.model.view;

import btrplace.JSONConverter;
import btrplace.JSONConverterException;
import btrplace.model.ModelView;
import net.minidev.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Extensible converter for {@link btrplace.model.ModelView}.
 *
 * @author Fabien Hermenier
 */
public class ModelViewsConverter implements JSONConverter<ModelView> {

    private Map<Class<? extends ModelView>, ModelViewConverter<? extends ModelView>> java2json;
    private Map<String, ModelViewConverter<? extends ModelView>> json2java;

    /**
     * Make a new converter.
     */
    public ModelViewsConverter() {
        java2json = new HashMap(20);
        json2java = new HashMap(20);

        //The default converters
        register(new ShareableResourceConverter());
    }

    /**
     * Register a converter for a specific view.
     *
     * @param c the converter to register
     * @return the container that was previously registered for a view. {@code null} if there was
     *         no registered converter
     */
    public ModelViewConverter register(ModelViewConverter<? extends ModelView> c) {
        java2json.put(c.getSupportedConstraint(), c);
        return json2java.put(c.getJSONId(), c);

    }

    /**
     * Get the Java view that are supported by the converter.
     *
     * @return a set of classes derived from {@link btrplace.model.ModelView} that may be empty
     */
    public Set<Class<? extends ModelView>> getSupportedJavaViews() {
        return java2json.keySet();
    }

    /**
     * Get the JSON views that are supported by the converter.
     *
     * @return a set of view identifiers that may be empty
     */
    public Set<String> getSupportedJSONViews() {
        return json2java.keySet();
    }

    @Override
    public ModelView fromJSON(JSONObject in) throws JSONConverterException {
        Object id = in.get("id");
        if (id == null) {
            throw new JSONConverterException("No 'id' key in the object to choose the converter to use");
        }
        ModelViewConverter<? extends ModelView> c = json2java.get(id.toString());
        if (c == null) {
            throw new JSONConverterException("No converter available for a view having id '" + id + "'");
        }
        return c.fromJSON(in);
    }

    @Override
    public JSONObject toJSON(ModelView o) throws JSONConverterException {
        ModelViewConverter c = java2json.get(o.getClass());
        if (c == null) {
            throw new JSONConverterException("No converter available for a view with the '" + o.getClass() + "' classname");
        }
        return c.toJSON(o);
    }
}
