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

package btrplace.model;

import btrplace.JSONConverter;
import btrplace.model.constraint.*;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Extensible converter for {@link SatConstraint}.
 *
 * @author Fabien Hermenier
 */
public class JSONSatConstraintsConverter implements JSONConverter<SatConstraint> {

    private Map<Class<? extends SatConstraint>, JSONSatConstraintConverter<? extends SatConstraint>> java2json;
    private Map<String, JSONSatConstraintConverter<? extends SatConstraint>> json2java;

    /**
     * Make a new converter.
     */
    public JSONSatConstraintsConverter() {
        java2json = new HashMap(20);
        json2java = new HashMap(20);

        //The default converters
        register(new JSONAmong());
        register(new JSONBan());
        register(new JSONCumulatedResourceCapacity());
        register(new JSONCumulatedRunningCapacity());
        register(new JSONFence());
        register(new JSONGather());
        register(new JSONKilled());
        register(new JSONLonely());
        register(new JSONOffline());
        register(new JSONOnline());
        register(new JSONOverbook());
        register(new JSONPreserve());
        register(new JSONQuarantine());
        register(new JSONReady());
        register(new JSONRoot());
        register(new JSONRunning());
        register(new JSONSequentialVMTransitions());
        register(new JSONSingleResourceCapacity());
        register(new JSONSingleRunningCapacity());
        register(new JSONSleeping());
        register(new JSONSplitAmong());
        register(new JSONSplit());
        register(new JSONSpread());
    }

    /**
     * Register a converter for a specific constraint.
     *
     * @param c the converter to register
     * @return the container that was previously registered for a constraint. {@code null} if there was
     *         no registered converter
     */
    public JSONSatConstraintConverter register(JSONSatConstraintConverter<? extends SatConstraint> c) {
        java2json.put(c.getSupportedConstraint(), c);
        return json2java.put(c.getJSONId(), c);

    }

    public Set<Class<? extends SatConstraint>> getSupportedConstraints() {
        return java2json.keySet();
    }

    @Override
    public SatConstraint fromJSON(JSONObject in) {
        String id = in.get("id").toString();
        JSONSatConstraintConverter<? extends SatConstraint> c = json2java.get(id);
        if (c == null) {
            return null;
        }
        return c.fromJSON(in);
    }

    @Override
    public JSONObject toJSON(SatConstraint o) {
        JSONSatConstraintConverter c = java2json.get(o.getClass());
        if (c == null) {
            return null;
        }
        return c.toJSON(o);
    }
}
