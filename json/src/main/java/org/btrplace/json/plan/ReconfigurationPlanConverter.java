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

package org.btrplace.json.plan;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.AbstractJSONObjectConverter;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.ModelConverter;
import org.btrplace.model.Model;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;

/**
 * JSON converter for {@link ReconfigurationPlan}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanConverter extends AbstractJSONObjectConverter<ReconfigurationPlan> {

    private ModelConverter mc;

    /**
     * Make a new converter that relies on a given ModelConverter
     *
     * @param c the model converter to rely on
     */
    public ReconfigurationPlanConverter(ModelConverter c) {
        this.mc = c;
    }

    /**
     * Make a new converter with the default {@link ModelConverter}.
     */
    public ReconfigurationPlanConverter() {
        this(new ModelConverter());
    }

    @Override
    public ReconfigurationPlan fromJSON(JSONObject ob) throws JSONConverterException {

        if (!ob.containsKey("origin")) {
            throw new JSONConverterException("Key 'origin' is expected to extract the source model from the plan");
        }

        if (!ob.containsKey("actions")) {
            throw new JSONConverterException("Key 'actions' is expected to extract the list of actions from the plan");
        }

        ActionConverter ac = new ActionConverter();
        Model m = mc.fromJSON((JSONObject) ob.get("origin"));
        ac.setModel(m);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(m);
        for (Action a : ac.listFromJSON((JSONArray) ob.get("actions"))) {
            plan.add(a);
        }
        return plan;
    }

    /**
     * Get the associated {@link ModelConverter}
     *
     * @return the converter provided at instantiation
     */
    public ModelConverter getModelConverter() {
        return mc;
    }

    @Override
    public JSONObject toJSON(ReconfigurationPlan plan) throws JSONConverterException {
        setModel(plan.getOrigin());
        JSONObject ob = new JSONObject();
        ActionConverter ac = new ActionConverter();
        Model src = plan.getOrigin();
        ob.put("origin", mc.toJSON(src));

        JSONArray actions = new JSONArray();
        for (Action a : plan.getActions()) {
            actions.add(ac.toJSON(a));
        }
        ob.put("actions", actions);
        return ob;
    }

}
