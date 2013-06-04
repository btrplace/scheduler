/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.json.model.constraint;

import btrplace.json.AbstractJSONObjectConverter;
import btrplace.json.JSONArrayConverter;
import btrplace.json.JSONConverterException;
import btrplace.model.constraint.SatConstraint;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.io.*;
import java.util.*;

/**
 * Extensible converter for {@link btrplace.model.constraint.SatConstraint}.
 *
 * @author Fabien Hermenier
 */
public class SatConstraintsConverter extends AbstractJSONObjectConverter<SatConstraint> implements JSONArrayConverter<SatConstraint> {

    private Map<Class<? extends SatConstraint>, SatConstraintConverter<? extends SatConstraint>> java2json;
    private Map<String, SatConstraintConverter<? extends SatConstraint>> json2java;

    /**
     * Make a new converter.
     */
    public SatConstraintsConverter() {
        java2json = new HashMap<>();
        json2java = new HashMap<>();

        //The default converters
        register(new AmongConverter());
        register(new BanConverter());
        register(new CumulatedResourceCapacityConverter());
        register(new CumulatedRunningCapacityConverter());
        register(new FenceConverter());
        register(new GatherConverter());
        register(new KilledConverter());
        register(new LonelyConverter());
        register(new OfflineConverter());
        register(new OnlineConverter());
        register(new OverbookConverter());
        register(new PreserveConverter());
        register(new QuarantineConverter());
        register(new ReadyConverter());
        register(new RootConverter());
        register(new RunningConverter());
        register(new SequentialVMTransitionsConverter());
        register(new SingleResourceCapacityConverter());
        register(new SingleRunningCapacityConverter());
        register(new SleepingConverter());
        register(new SplitAmongConverter());
        register(new SplitConverter());
        register(new SpreadConverter());
    }

    /**
     * Register a converter for a specific constraint.
     *
     * @param c the converter to register
     * @return the container that was previously registered for a constraint. {@code null} if there was
     *         no registered converter
     */
    public SatConstraintConverter register(SatConstraintConverter<? extends SatConstraint> c) {
        java2json.put(c.getSupportedConstraint(), c);
        return json2java.put(c.getJSONId(), c);

    }

    /**
     * Get the Java constraints that are supported by the converter.
     *
     * @return a set of classes derived from {@link SatConstraint} that may be empty
     */
    public Set<Class<? extends SatConstraint>> getSupportedJavaConstraints() {
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
    public SatConstraint fromJSON(JSONObject in) throws JSONConverterException {
        Object id = in.get("id");
        if (id == null) {
            throw new JSONConverterException("No 'id' key in the object to choose the converter to use");
        }
        SatConstraintConverter<? extends SatConstraint> c = json2java.get(id.toString());
        if (c == null) {
            throw new JSONConverterException("No converter available for a constraint having id '" + id + "'");
        }
        c.setModel(getModel());
        return c.fromJSON(in);
    }

    @Override
    public JSONObject toJSON(SatConstraint o) throws JSONConverterException {
        SatConstraintConverter c = java2json.get(o.getClass());
        if (c == null) {
            throw new JSONConverterException("No converter available for a constraint with the '" + o.getClass() + "' classname");
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
            l.add(fromJSON((JSONObject) o));
        }
        return l;
    }

    @Override
    public JSONArray toJSON(Collection<SatConstraint> e) throws JSONConverterException {
        JSONArray arr = new JSONArray();
        for (SatConstraint cstr : e) {
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
    public List<SatConstraint> listFromJSON(String buf) throws IOException, JSONConverterException {
        try (StringReader in = new StringReader(buf)) {
            return listFromJSON(in);
        }
    }

    @Override
    public List<SatConstraint> listFromJSON(Reader r) throws IOException, JSONConverterException {
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
