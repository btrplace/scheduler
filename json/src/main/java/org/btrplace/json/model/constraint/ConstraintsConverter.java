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

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.btrplace.json.AbstractJSONObjectConverter;
import org.btrplace.json.JSONArrayConverter;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.SatConstraint;

import java.io.*;
import java.util.*;

/**
 * Extensible converter for {@link org.btrplace.model.constraint.Constraint}.
 *
 * @author Fabien Hermenier
 */
public class ConstraintsConverter extends AbstractJSONObjectConverter<Constraint> implements JSONArrayConverter<SatConstraint> {

    private Map<Class<? extends Constraint>, ConstraintConverter<? extends Constraint>> java2json;
    private Map<String, ConstraintConverter<? extends Constraint>> json2java;

    /**
     * Make a new empty converter.
     */
    public ConstraintsConverter() {
        java2json = new HashMap<>();
        json2java = new HashMap<>();
    }

    /**
     * Make a new {@code ConstraintsConverter} and fulfill it
     * using a default converter for each supported constraint.
     *
     * @return a fulfilled converter.
     */
    public static ConstraintsConverter newBundle() {
        //The default converters
        ConstraintsConverter c = new ConstraintsConverter();
        c.register(new AmongConverter());
        c.register(new BanConverter());
        c.register(new ResourceCapacityConverter());
        c.register(new RunningCapacityConverter());
        c.register(new FenceConverter());
        c.register(new GatherConverter());
        c.register(new KilledConverter());
        c.register(new LonelyConverter());
        c.register(new OfflineConverter());
        c.register(new OnlineConverter());
        c.register(new OverbookConverter());
        c.register(new PreserveConverter());
        c.register(new QuarantineConverter());
        c.register(new ReadyConverter());
        c.register(new RootConverter());
        c.register(new RunningConverter());
        c.register(new SeqConverter());
        c.register(new SleepingConverter());
        c.register(new SplitAmongConverter());
        c.register(new SplitConverter());
        c.register(new SpreadConverter());
        c.register(new MaxOnlineConverter());
        c.register(new MinMTTRConverter());
        return c;
    }

    /**
     * Register a converter for a specific constraint.
     *
     * @param c the converter to register
     * @return the container that was previously registered for a constraint. {@code null} if there was
     * no registered converter
     */
    public ConstraintConverter register(ConstraintConverter<? extends Constraint> c) {
        java2json.put(c.getSupportedConstraint(), c);
        return json2java.put(c.getJSONId(), c);

    }

    /**
     * Get the Java constraints that are supported by the converter.
     *
     * @return a set of classes derived from {@link Constraint} that may be empty
     */
    public Set<Class<? extends Constraint>> getSupportedJavaConstraints() {
        return java2json.keySet();
    }

    /**
     * Get the JSON constraints that are supported by the converter.
     *
     * @return a set of constraints identifier that may be empty
     */
    public Set<String> getSupportedJSONConstraints() {
        return json2java.keySet();
    }

    @Override
    public Constraint fromJSON(JSONObject in) throws JSONConverterException {
        Object id = in.get("id");
        if (id == null) {
            throw new JSONConverterException("No 'id' key in the object to choose the converter to use");
        }
        ConstraintConverter<? extends Constraint> c = json2java.get(id.toString());
        if (c == null) {
            throw new JSONConverterException("No converter available for a constraint having id '" + id + "'");
        }
        c.setModel(getModel());
        return c.fromJSON(in);
    }

    @Override
    public JSONObject toJSON(Constraint o) throws JSONConverterException {
        ConstraintConverter c = java2json.get(o.getClass());
        if (c == null) {
            throw new JSONConverterException("No converter available for a constraint with the '" + o.getClass() + "' className");
        }
        return c.toJSON(o);
    }

    @Override
    public List<SatConstraint> listFromJSON(JSONArray in) throws JSONConverterException {
        List<SatConstraint> l = new ArrayList<>(in.size());
        for (Object o : in) {
            if (!(o instanceof JSONObject)) {
                throw new JSONConverterException("Expected an array of JSONObject but got an array of " + o.getClass().getName());
            }
            l.add((SatConstraint) fromJSON((JSONObject) o));
        }
        return l;
    }

    @Override
    public JSONArray toJSON(Collection<SatConstraint> e) throws JSONConverterException {
        JSONArray arr = new JSONArray();
        for (Constraint cstr : e) {
            arr.add(toJSON(cstr));
        }
        return arr;
    }

    @Override
    public List<SatConstraint> listFromJSON(File path) throws IOException, JSONConverterException {
        try (BufferedReader in = new BufferedReader(new FileReader(path))) {
            return listFromJSON(in);
        }

    }

    @Override
    public List<SatConstraint> listFromJSON(String buf) throws JSONConverterException {
        try (StringReader in = new StringReader(buf)) {
            return listFromJSON(in);
        }
    }

    @Override
    public List<SatConstraint> listFromJSON(Reader r) throws JSONConverterException {
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
    public String toJSONString(Collection<SatConstraint> o) throws JSONConverterException {
        return toJSON(o).toJSONString();
    }

    @Override
    public void toJSON(Collection<SatConstraint> e, Appendable w) throws JSONConverterException, IOException {
        toJSON(e).writeJSONString(w);
    }

    @Override
    public void toJSON(Collection<SatConstraint> e, File path) throws JSONConverterException, IOException {
        try (FileWriter out = new FileWriter(path)) {
            toJSON(e, out);
        }
    }
}
