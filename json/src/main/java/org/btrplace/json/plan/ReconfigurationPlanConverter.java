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
import org.btrplace.json.JSONs;
import org.btrplace.json.model.ModelConverter;
import org.btrplace.model.Model;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.Action.Hook;
import org.btrplace.plan.event.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON converter for {@link ReconfigurationPlan}.
 * By default, the converter does not support any kind of actions or events.
 * {@link #newBundle()} provides a fully configured converter that supports all
 * the actions and events bundled in btrplace. To register converters for new
 * actions or events, use {@link #register(EventConverter)} and
 * {@link #register(ActionConverter)}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanConverter implements
    JSONObjectConverter<ReconfigurationPlan> {

    private final ModelConverter mc;

    private final Map<Class<? extends Event>,
        EventConverter<? extends Event>> java2json;
    private final Map<String, EventConverter<? extends Event>> json2java;

    private final Map<Class<? extends Action>, ActionConverter<?
        extends Action>> java3json;
    private final Map<String, ActionConverter<? extends Action>> json3java;

    /**
     * Key that indicates the origin model.
     */
    public static final String ORIGIN_LABEL = "origin";

    /**
     * Key that indicates the actions.
     */
    public static final String ACTIONS_LABEL = "actions";

    /**
     * Ket that indicates the hooks.
     */
    public static final String HOOK_LABEL = "hooks";

    /**
     * Make a new converter that relies on a given ModelConverter
     *
     * @param c the model converter to rely on
     */
    public ReconfigurationPlanConverter(ModelConverter c) {
        this.mc = c;
        java2json = new HashMap<>();
        json2java = new HashMap<>();

        java3json = new HashMap<>();
        json3java = new HashMap<>();
    }

    /**
     * Make a new converter with the default {@link ModelConverter}.
     */
    public ReconfigurationPlanConverter() {
        this(new ModelConverter());
    }

    @Override
    public ReconfigurationPlan fromJSON(JSONObject ob)
        throws JSONConverterException {

        JSONs.checkKeys(ob, ORIGIN_LABEL, ACTIONS_LABEL);
        final Model m = mc.fromJSON((JSONObject) ob.get(ORIGIN_LABEL));
        final ReconfigurationPlan plan = new DefaultReconfigurationPlan(m);
        for (final JSONObject json : (List<JSONObject>) ob.get(ACTIONS_LABEL)) {
            final String id =
                json.getAsString(ActionConverter.ID_LABEL);
            ActionConverter<? extends Action> ac = json3java.get(id);
            if (ac == null) {
                throw new JSONConverterException(
                    "No converter for action '" + id + "'");
            }
            final Action action = ac.fromJSON(m, json);
            eventsFromJSON(json, m, action);
            plan.add(action);
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
    public JSONObject toJSON(ReconfigurationPlan plan)
        throws JSONConverterException {

        final JSONObject ob = new JSONObject();
        final Model src = plan.getOrigin();
        ob.put(ORIGIN_LABEL, mc.toJSON(src));

        final JSONArray actions = new JSONArray();
        for (final Action a : plan.getActions()) {
            final ActionConverter ac = java3json.get(a.getClass());
            if (ac == null) {
                throw new JSONConverterException(
                    "No converter registered for '" + a.getClass() + "'");
            }
            final JSONObject json = ac.toJSON(a);
            eventsToJSON(a, json);
            actions.add(json);
        }
        ob.put(ACTIONS_LABEL, actions);
        return ob;
    }

    private void eventsToJSON(final Action action, final JSONObject json)
        throws JSONConverterException {

        final JSONObject hooks = new JSONObject();
        for (final Hook k : Hook.values()) {
            final JSONArray arr = new JSONArray();
            for (final Event ev : action.getEvents(k)) {
                final EventConverter c = java2json.get(ev.getClass());
                if (c == null) {
                    throw new JSONConverterException("No converter " +
                        "registered for '" + ev + "'");
                }
                arr.add(c.toJSON(ev));
            }
            hooks.put(k.toString(), arr);
        }
        json.put(HOOK_LABEL, hooks);
    }

    private void eventsFromJSON(final JSONObject json,
                                final Model mo, final Action action)
        throws JSONConverterException {

        final JSONObject hooks =
            (JSONObject) json.getOrDefault(HOOK_LABEL, new JSONObject());
        for (Map.Entry<String, Object> e : hooks.entrySet()) {
            String k = e.getKey();
            try {
                Hook h = Hook.valueOf(k.toUpperCase());
                for (Object o : (JSONArray) e.getValue()) {
                    action.addEvent(h, eventFromJSON(mo, o));
                }
            } catch (IllegalArgumentException ex) {
                throw new JSONConverterException(
                    "Unsupported hook type '" + k + "'", ex);
            }
        }
    }

    public void register(final EventConverter<? extends Event> ec) {
        java2json.put(ec.supportedEvent(), ec);
        json2java.put(ec.id(), ec);
    }

    public void register(final ActionConverter<? extends Action> ec) {
        java3json.put(ec.supportedAction(), ec);
        json3java.put(ec.id(), ec);
    }

    private Event eventFromJSON(final Model mo, final Object o)
        throws JSONConverterException {

        final JSONObject json = (JSONObject) o;
        JSONs.checkKeys(json, ActionConverter.ID_LABEL);
        final String id = json.get(ActionConverter.ID_LABEL).toString();
        final EventConverter<? extends Event> ec = json2java.get(id);
        if (ec == null) {
            throw new JSONConverterException(
                "No converter available for a event having id '" + id + "'");
        }
        return ec.fromJSON(mo, json);
    }

    public static ReconfigurationPlanConverter newBundle() {
        final ReconfigurationPlanConverter conv =
            new ReconfigurationPlanConverter();
        conv.register(new AllocateEventConverter());
        conv.register(new SubstituteVMEventConverter());

        conv.register(new BootNodeConverter());
        conv.register(new ShutdownNodeConverter());
        conv.register(new MigrateVMConverter());
        conv.register(new SuspendVMConverter());
        conv.register(new ResumeVMConverter());
        conv.register(new AllocateConverter());
        conv.register(new BootVMConverter());
        conv.register(new ShutdownVMConverter());
        conv.register(new KillVMConverter());
        return conv;
    }
}
