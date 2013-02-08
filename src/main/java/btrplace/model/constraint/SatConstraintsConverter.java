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

package btrplace.model.constraint;

import btrplace.JSONConverter;
import btrplace.JSONConverterException;
import btrplace.model.SatConstraint;
import net.minidev.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Extensible converter for {@link btrplace.model.SatConstraint}.
 *
 * @author Fabien Hermenier
 */
public class SatConstraintsConverter implements JSONConverter<SatConstraint> {

    private Map<Class<? extends SatConstraint>, SatConstraintConverter<? extends SatConstraint>> java2json;
    private Map<String, SatConstraintConverter<? extends SatConstraint>> json2java;

    /**
     * Make a new converter.
     */
    public SatConstraintsConverter() {
        java2json = new HashMap(20);
        json2java = new HashMap(20);

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
            throw new JSONConverterException("No converter available for a constraint with id '" + id + "'");
        }
        return c.fromJSON(in);
    }

    @Override
    public JSONObject toJSON(SatConstraint o) {
        SatConstraintConverter c = java2json.get(o.getClass());
        if (c == null) {
            return null;
        }
        return c.toJSON(o);
    }
}
