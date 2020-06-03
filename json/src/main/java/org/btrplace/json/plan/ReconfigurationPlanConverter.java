/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.plan;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.JSONObjectConverter;
import org.btrplace.json.model.ModelConverter;
import org.btrplace.model.Model;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;

import static org.btrplace.json.JSONs.checkKeys;

/**
 * JSON converter for {@link ReconfigurationPlan}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanConverter implements JSONObjectConverter<ReconfigurationPlan> {

    private final ModelConverter mc;

    /**
     * Key that indicates the origin model.
     */
    public static final String ORIGIN_LABEL = "origin";

    /**
     * Key that indicates the actions.
     */
    public static final String ACTIONS_LABEL = "actions";

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
        checkKeys(ob, ORIGIN_LABEL, ACTIONS_LABEL);
        Model m = mc.fromJSON((JSONObject) ob.get(ORIGIN_LABEL));
        ActionConverter ac = new ActionConverter(m);
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(m);
        for (Action a : ac.listFromJSON((JSONArray) ob.get(ACTIONS_LABEL))) {
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
        JSONObject ob = new JSONObject();
        Model src = plan.getOrigin();
        ActionConverter ac = new ActionConverter(src);
        ob.put(ORIGIN_LABEL, mc.toJSON(src));

        JSONArray actions = new JSONArray();
        for (Action a : plan.getActions()) {
            actions.add(ac.toJSON(a));
        }
        ob.put(ACTIONS_LABEL, actions);
        return ob;
    }

}
