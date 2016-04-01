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

package org.btrplace.json.model;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.btrplace.json.AbstractJSONObjectConverter;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.constraint.ConstraintsConverter;
import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.OptConstraint;
import org.btrplace.model.constraint.SatConstraint;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

/**
 * A JSON converter for {@link org.btrplace.model.Instance}.
 *
 * @author Fabien Hermenier
 */

public class InstanceConverter extends AbstractJSONObjectConverter<Instance> {

    /**
     * Quick deserialization for a pure legacy BtrPlace instance.
     *
     * @param in the json version of the instance
     * @return the resulting instance
     * @throws IllegalArgumentException if the json format is incorrect
     */
    public static Instance quickFromJSON(JSONObject in) {
        try {
            return new InstanceConverter().fromJSON(in);
        } catch (JSONConverterException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Quick deserialization for a pure legacy BtrPlace instance.
     * The file must be encoded in UTF-8.
     * @param path the file containing the json message. If the file name ends with ".gz", a gzipped file is assumed
     * @return the resulting instance
     * @throws IllegalArgumentException if the json format is incorrect
     */
    public static Instance quickFromJSON(File path) throws IllegalArgumentException {
        if (path.getName().endsWith(".gz")) {
            try (Reader in = new InputStreamReader(new GZIPInputStream(new FileInputStream(path)), StandardCharsets.UTF_8)) {
                return quickFromJSON(in);
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        try (Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            return quickFromJSON(in);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Quick deserialization for a pure legacy BtrPlace instance.
     *
     * @param buf the json message
     * @return the resulting instance
     * @throws IllegalArgumentException if the json format is incorrect
     */
    public static Instance quickFromJSON(String buf) throws IllegalArgumentException {
        try (StringReader in = new StringReader(buf)) {
            return quickFromJSON(in);
        }
    }

    public static Instance quickFromJSON(Reader r) throws IllegalArgumentException {
        try {
            JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
            Object o = p.parse(r);
            if (!(o instanceof JSONObject)) {
                throw new IllegalArgumentException("Unable to parse a JSON object");
            }
            return quickFromJSON((JSONObject) o);
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Quick serialization of a pure legacy BtrPlace instance.
     *
     * @param in the instance
     * @return the resulting JSON Object
     * @throws IllegalArgumentException if the json format is incorrect
     */
    public static JSONObject quickToJSON(Instance in) {
        try {
            return new InstanceConverter().toJSON(in);
        } catch (JSONConverterException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Quick serialization of a pure legacy BtrPlace instance.
     *
     * @param mo    the model
     * @param cstrs the sat-constraints
     * @param o     the optimisation constraint
     * @return {@code quickToJSON(new Instance(mo, cstrs, o)}
     * @throws IllegalArgumentException if the json format is incorrect
     */
    public static JSONObject quickToJSON(Model mo, Collection<SatConstraint> cstrs, OptConstraint o) {
        return quickToJSON(new Instance(mo, cstrs, o));
    }

    @Override
    public Instance fromJSON(JSONObject in) throws JSONConverterException {
        ModelConverter moc = new ModelConverter();
        moc.setCharset(getCharset());
        ConstraintsConverter cConverter = ConstraintsConverter.newBundle();
        cConverter.setCharset(getCharset());
        Model mo = moc.fromJSON((JSONObject) in.get("model"));
        cConverter.setModel(mo);
        return new Instance(mo, cConverter.listFromJSON((JSONArray) in.get("constraints")),
                (OptConstraint) cConverter.fromJSON((JSONObject) in.get("objective")));
    }

    @Override
    public JSONObject toJSON(Instance instance) throws JSONConverterException {
        ModelConverter moc = new ModelConverter();
        moc.setCharset(getCharset());
        ConstraintsConverter cstrc = ConstraintsConverter.newBundle();
        cstrc.setCharset(getCharset());
        JSONObject ob = new JSONObject();
        ob.put("model", moc.toJSON(instance.getModel()));
        ob.put("constraints", cstrc.toJSON(instance.getSatConstraints()));
        ob.put("objective", cstrc.toJSON(instance.getOptConstraint()));
        return ob;
    }
}
