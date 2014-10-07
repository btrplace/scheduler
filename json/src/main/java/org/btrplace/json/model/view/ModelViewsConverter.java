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

package org.btrplace.json.model.view;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.btrplace.json.AbstractJSONObjectConverter;
import org.btrplace.json.JSONArrayConverter;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.view.ModelView;

import java.io.*;
import java.util.*;

/**
 * Extensible converter for {@link org.btrplace.model.view.ModelView}.
 *
 * @author Fabien Hermenier
 */
public class ModelViewsConverter extends AbstractJSONObjectConverter<ModelView> implements JSONArrayConverter<ModelView> {

    private Map<Class<? extends ModelView>, ModelViewConverter<? extends ModelView>> java2json;
    private Map<String, ModelViewConverter<? extends ModelView>> json2java;

    /**
     * Make a new empty converter.
     */
    public ModelViewsConverter() {
        java2json = new HashMap<>();
        json2java = new HashMap<>();
    }

    /**
     * Make a new {@code ModelViewsConverter} and fulfill it using converters for the following views:
     * <ul>
     * <li>{@link org.btrplace.json.model.view.ShareableResourceConverter}</li>
     * <li>{@link org.btrplace.json.model.view.NamingServiceConverter}</li>
     * </ul>
     *
     * @return a fulfilled converter.
     */
    public static ModelViewsConverter newBundle() {
        ModelViewsConverter converter = new ModelViewsConverter();
        converter.register(new ShareableResourceConverter());
        converter.register(new NamingServiceConverter());
        return converter;
    }

    /**
     * Register a converter for a specific view.
     *
     * @param c the converter to register
     * @return the container that was previously registered for a view. {@code null} if there was
     * no registered converter
     */
    public ModelViewConverter register(ModelViewConverter<? extends ModelView> c) {
        java2json.put(c.getSupportedView(), c);
        return json2java.put(c.getJSONId(), c);

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
        c.setModel(getModel());
        return c.fromJSON(in);
    }

    @Override
    public JSONObject toJSON(ModelView o) throws JSONConverterException {
        ModelViewConverter c = java2json.get(o.getClass());
        if (c == null) {
            throw new JSONConverterException("No converter available for a view with the '" + o.getClass() + "' className");
        }
        return c.toJSON(o);
    }

    @Override
    public List<ModelView> listFromJSON(JSONArray in) throws JSONConverterException {
        List<ModelView> l = new ArrayList<>(in.size());
        for (Object o : in) {
            if (!(o instanceof JSONObject)) {
                throw new JSONConverterException("Expected an array of JSONObject but got an array of " + o.getClass().getName());
            }
            l.add(fromJSON((JSONObject) o));
        }
        return l;
    }

    @Override
    public JSONArray toJSON(Collection<ModelView> e) throws JSONConverterException {
        JSONArray arr = new JSONArray();
        for (ModelView cstr : e) {
            arr.add(toJSON(cstr));
        }
        return arr;
    }

    @Override
    public List<ModelView> listFromJSON(File path) throws IOException, JSONConverterException {
        try (BufferedReader in = new BufferedReader(new FileReader(path))) {
            return listFromJSON(in);
        }

    }

    @Override
    public List<ModelView> listFromJSON(String buf) throws JSONConverterException {
        try (StringReader in = new StringReader(buf)) {
            return listFromJSON(in);
        }
    }

    @Override
    public List<ModelView> listFromJSON(Reader r) throws JSONConverterException {
        try {
            JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
            Object o = p.parse(r);
            if (!(o instanceof JSONArray)) {
                throw new JSONConverterException("Unable to parse a JSONArray");
            }
            return listFromJSON((JSONArray) o);
        } catch (ParseException ex) {
            throw new JSONConverterException(ex);
        }
    }

    @Override
    public String toJSONString(Collection<ModelView> o) throws JSONConverterException {
        return toJSON(o).toJSONString();
    }

    @Override
    public void toJSON(Collection<ModelView> e, Appendable w) throws JSONConverterException, IOException {
        toJSON(e).writeJSONString(w);
    }

    @Override
    public void toJSON(Collection<ModelView> e, File path) throws JSONConverterException, IOException {
        try (FileWriter out = new FileWriter(path)) {
            toJSON(e, out);
        }
    }


}
