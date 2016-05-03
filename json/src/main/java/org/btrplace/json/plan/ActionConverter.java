/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.JSONs;
import org.btrplace.model.Model;
import org.btrplace.plan.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.btrplace.json.JSONs.*;

/**
 * JSON converter for {@link Action}.
 *
 * @author Fabien Hermenier
 */
public class ActionConverter implements ActionVisitor {

    /**
     * Key that indicates the beginning an action.
     */
    public static final String START_LABEL = "start";

    /**
     * Key that indicates the end an action.
     */
    public static final String END_LABEL = "end";

    /**
     * Key that indicates a VM identifier.
     */
    public static final String VM_LABEL = "vm";

    /**
     * Key that indicates a node identifier.
     */
    public static final String NODE_LABEL = "node";

    /**
     * Key that indicates the current position of a VM.
     */
    public static final String ON_LABEL = "on";

    /**
     * Ket that indicates the hooks.
     */
    public static final String HOOK_LABEL = "hooks";

    /**
     * Key that indicate the action identifier
     */
    public static final String ACTION_ID_LABEL = "id";

    public static final String VM_LOCATION_LABEL = "from";

    public static final String VM_DESTINATION_LABEL = "to";

    public static final String BANDWIDTH = "bw";

    /**
     * Key to indicate a resource identifier.
     */
    public static final String RC_LABEL = "rc";

    /**
     * Key to indicate a resource amount.
     */
    public static final String RC_AMOUNT_LABEL = "amount";

    private Model mo;

    /**
     * A new converter
     *
     * @param mo the model to rely on.
     */
    public ActionConverter(Model mo) {
        this.mo = mo;
    }

    /**
     * decode a json-encoded action.
     * @param in the action to decode
     * @return the resulting action
     * @throws JSONConverterException if the conversion failed
     */
    public Action fromJSON(JSONObject in) throws JSONConverterException {
        String id = requiredString(in, ACTION_ID_LABEL);
        Action a;

        switch (id) {
            case "bootVM":
                a = bootVMFromJSON(in);
                break;
            case "shutdownVM":
                a = shutdownVMFromJSON(in);
                break;
            case "shutdownNode":
                a = shutdownNodeFromJSON(in);
                break;
            case "bootNode":
                a = bootNodeFromJSON(in);
                break;
            case "forgeVM":
                a = forgeVMFromJSON(in);
                break;
            case "killVM":
                a = killVMFromJSON(in);
                break;
            case "migrateVM":
                a = migrateVMFromJSON(in);
                break;
            case "resumeVM":
                a = resumeVMFromJSON(in);
                break;
            case "suspendVM":
                a = suspendVMFromJSON(in);
                break;
            case "allocate":
                a = allocateFromJSON(in);
                break;
            default:
                throw new JSONConverterException("Unsupported action '" + id + "'");
        }

        attachEvents(a, in);
        return a;
    }

    /**
     * Decorate the action with optional events.
     *
     * @param a  the action to decorate
     * @param in the JSON message containing the event at the "hook" key
     * @throws JSONConverterException in case of error
     */
    private void attachEvents(Action a, JSONObject in) throws JSONConverterException {
        if (in.containsKey(HOOK_LABEL)) {
            JSONObject hooks = (JSONObject) in.get(HOOK_LABEL);
            for (Map.Entry<String, Object> e : hooks.entrySet()) {
                String k = e.getKey();
                try {
                    Action.Hook h = Action.Hook.valueOf(k.toUpperCase());
                    for (Object o : (JSONArray) e.getValue()) {
                        a.addEvent(h, eventFromJSON((JSONObject) o));
                    }
                } catch (IllegalArgumentException ex) {
                    throw new JSONConverterException("Unsupported hook type '" + k + "'", ex);
                }
            }
        }
    }

    private Event eventFromJSON(JSONObject o) throws JSONConverterException {
        String id = requiredString(o, ACTION_ID_LABEL);

        switch (id) {
            case "allocate":
                return allocateEventFromJSON(o);
            case "substitutedVM":
                return substitutedVMEventFromJSON(o);
            default:
                throw new JSONConverterException("Unsupported type of action '" + id + "'");
        }
    }


    @Override
    public JSONObject visit(BootVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "bootVM");
        o.put(VM_LABEL, JSONs.toJSON(a.getVM()));
        o.put(VM_DESTINATION_LABEL, JSONs.toJSON(a.getDestinationNode()));
        return o;
    }

    private BootVM bootVMFromJSON(JSONObject in) throws JSONConverterException {
        return new BootVM(requiredVM(mo, in, VM_LABEL),
                requiredNode(mo, in, VM_DESTINATION_LABEL),
                requiredInt(in, START_LABEL),
                requiredInt(in, END_LABEL));
    }

    @Override
    public JSONObject visit(ShutdownVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "shutdownVM");
        o.put(VM_LABEL, JSONs.toJSON(a.getVM()));
        o.put(ON_LABEL, JSONs.toJSON(a.getNode()));
        return o;
    }

    private ShutdownVM shutdownVMFromJSON(JSONObject in) throws JSONConverterException {
        return new ShutdownVM(requiredVM(mo, in, VM_LABEL),
                requiredNode(mo, in, ON_LABEL),
                requiredInt(in, START_LABEL),
                requiredInt(in, END_LABEL));
    }

    @Override
    public JSONObject visit(ShutdownNode a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "shutdownNode");
        o.put(NODE_LABEL, JSONs.toJSON(a.getNode()));
        return o;
    }

    private ShutdownNode shutdownNodeFromJSON(JSONObject in) throws JSONConverterException {
        return new ShutdownNode(requiredNode(mo, in, NODE_LABEL),
                requiredInt(in, START_LABEL),
                requiredInt(in, END_LABEL));
    }

    @Override
    public JSONObject visit(BootNode a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "bootNode");
        o.put(NODE_LABEL, JSONs.toJSON(a.getNode()));
        return o;
    }

    private BootNode bootNodeFromJSON(JSONObject in) throws JSONConverterException {
        return new BootNode(requiredNode(mo, in, NODE_LABEL),
                requiredInt(in, START_LABEL),
                requiredInt(in, END_LABEL));
    }

    @Override
    public JSONObject visit(MigrateVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "migrateVM");
        o.put(VM_LABEL, JSONs.toJSON(a.getVM()));
        o.put(VM_DESTINATION_LABEL, JSONs.toJSON(a.getDestinationNode()));
        o.put(VM_LOCATION_LABEL, JSONs.toJSON(a.getSourceNode()));
        o.put(BANDWIDTH, a.getBandwidth());
        return o;
    }


    private MigrateVM migrateVMFromJSON(JSONObject in) throws JSONConverterException {
        return new MigrateVM(requiredVM(mo, in, VM_LABEL),
                requiredNode(mo, in, VM_LOCATION_LABEL),
                requiredNode(mo, in, VM_DESTINATION_LABEL),
                requiredInt(in, START_LABEL),
                requiredInt(in, END_LABEL),
                optInt(in, BANDWIDTH, Integer.MAX_VALUE)
                );
    }

    @Override
    public JSONObject visit(SuspendVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "suspendVM");
        o.put(VM_LABEL, JSONs.toJSON(a.getVM()));
        o.put(VM_DESTINATION_LABEL, JSONs.toJSON(a.getDestinationNode()));
        o.put(VM_LOCATION_LABEL, JSONs.toJSON(a.getSourceNode()));
        return o;
    }

    private SuspendVM suspendVMFromJSON(JSONObject in) throws JSONConverterException {
        return new SuspendVM(requiredVM(mo, in, VM_LABEL),
                requiredNode(mo, in, VM_LOCATION_LABEL),
                requiredNode(mo, in, VM_DESTINATION_LABEL),
                requiredInt(in, START_LABEL),
                requiredInt(in, END_LABEL));
    }

    @Override
    public JSONObject visit(ResumeVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "resumeVM");
        o.put(VM_LABEL, JSONs.toJSON(a.getVM()));
        o.put(VM_DESTINATION_LABEL, JSONs.toJSON(a.getDestinationNode()));
        o.put(VM_LOCATION_LABEL, JSONs.toJSON(a.getSourceNode()));
        return o;
    }

    private ResumeVM resumeVMFromJSON(JSONObject in) throws JSONConverterException {
        return new ResumeVM(requiredVM(mo, in, VM_LABEL),
                requiredNode(mo, in, VM_LOCATION_LABEL),
                requiredNode(mo, in, VM_DESTINATION_LABEL),
                requiredInt(in, START_LABEL),
                requiredInt(in, END_LABEL));
    }

    @Override
    public JSONObject visit(KillVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "killVM");
        o.put(VM_LABEL, JSONs.toJSON(a.getVM()));
        o.put(ON_LABEL, JSONs.toJSON(a.getNode()));
        return o;
    }

    private KillVM killVMFromJSON(JSONObject in) throws JSONConverterException {
        return new KillVM(requiredVM(mo, in, VM_LABEL),
                requiredNode(mo, in, ON_LABEL),
                requiredInt(in, START_LABEL),
                requiredInt(in, END_LABEL));

    }

    @Override
    public JSONObject visit(ForgeVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "forgeVM");
        o.put(VM_LABEL, JSONs.toJSON(a.getVM()));
        return o;

    }

    private ForgeVM forgeVMFromJSON(JSONObject in) throws JSONConverterException {
        return new ForgeVM(requiredVM(mo, in, VM_LABEL),
                requiredInt(in, START_LABEL),
                requiredInt(in, END_LABEL));
    }

    @Override
    public JSONObject visit(Allocate a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "allocate");
        o.put(VM_LABEL, JSONs.toJSON(a.getVM()));
        o.put(RC_LABEL, a.getResourceId());
        o.put(RC_AMOUNT_LABEL, a.getAmount());
        o.put(ON_LABEL, JSONs.toJSON(a.getHost()));
        return o;
    }

    private Allocate allocateFromJSON(JSONObject in) throws JSONConverterException {
        return new Allocate(requiredVM(mo, in, VM_LABEL),
                requiredNode(mo, in, ON_LABEL),
                requiredString(in, RC_LABEL),
                requiredInt(in, RC_AMOUNT_LABEL),
                requiredInt(in, START_LABEL),
                requiredInt(in, END_LABEL));
    }

    @Override
    public Object visit(AllocateEvent a) {
        JSONObject o = new JSONObject();
        o.put(ACTION_ID_LABEL, "allocate");
        o.put(RC_LABEL, a.getResourceId());
        o.put(VM_LABEL, JSONs.toJSON(a.getVM()));
        o.put(RC_AMOUNT_LABEL, a.getAmount());
        return o;
    }

    private AllocateEvent allocateEventFromJSON(JSONObject o) throws JSONConverterException {
        return new AllocateEvent(requiredVM(mo, o, VM_LABEL),
                requiredString(o, RC_LABEL),
                requiredInt(o, RC_AMOUNT_LABEL));
    }

    @Override
    public Object visit(SubstitutedVMEvent a) {
        JSONObject o = new JSONObject();
        o.put(ACTION_ID_LABEL, "substitutedVM");
        o.put(VM_LABEL, JSONs.toJSON(a.getVM()));
        o.put("newVm", JSONs.toJSON(a.getNewVM()));
        return o;
    }

    private SubstitutedVMEvent substitutedVMEventFromJSON(JSONObject o) throws JSONConverterException {
        return new SubstitutedVMEvent(requiredVM(mo, o, VM_LABEL),
                requiredVM(mo, o, "newVm"));
    }

    /**
     * Serialise an action.
     * @param a the action
     * @return the resulting encoded action
     * @throws JSONConverterException if the conversion exploded
     */
    public JSONObject toJSON(Action a) throws JSONConverterException {
        return (JSONObject) a.visit(this);
    }

    /**
     * Just create the JSONObject and set the consume and the end attribute.
     *
     * @param a the action to convert
     * @return a skeleton JSONObject
     */
    private JSONObject makeActionSkeleton(Action a) {
        JSONObject o = new JSONObject();
        o.put(START_LABEL, a.getStart());
        o.put(END_LABEL, a.getEnd());
        JSONObject hooks = new JSONObject();
        for (Action.Hook k : Action.Hook.values()) {
            JSONArray arr = new JSONArray();
            for (Event e : a.getEvents(k)) {
                arr.add(toJSON(e));
            }
            hooks.put(k.toString(), arr);
        }
        o.put(HOOK_LABEL, hooks);
        return o;
    }


    private JSONObject toJSON(Event e) {
        return (JSONObject) e.visit(this);
    }

    /**
     * Convert a list of json-encoded actions.
     * @param in the list to decode
     * @return the action list. Might be empty
     * @throws JSONConverterException if the conversion failed
     */
    public List<Action> listFromJSON(JSONArray in) throws JSONConverterException {
        List<Action> l = new ArrayList<>(in.size());
        for (Object o : in) {
            if (!(o instanceof JSONObject)) {
                throw new JSONConverterException("Expected an array of JSONObject but got an array of " + o.getClass().getName());
            }
            l.add(fromJSON((JSONObject) o));
        }
        return l;
    }
}
