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

package btrplace.json.plan;

import btrplace.json.AbstractJSONObjectConverter;
import btrplace.json.JSONArrayConverter;
import btrplace.json.JSONConverterException;
import btrplace.plan.event.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JSON converter for {@link Action}.
 *
 * @author Fabien Hermenier
 */
public class ActionConverter extends AbstractJSONObjectConverter<Action> implements ActionVisitor, JSONArrayConverter<Action> {

    /**
     * Key that indicate the beginning an action.
     */
    public static final String START_LABEL = "start";

    /**
     * Key that indicate the end an action.
     */
    public static final String END_LABEL = "end";

    /**
     * Key that indicate a VM identifier.
     */
    public static final String VM_LABEL = "vm";

    /**
     * Key that indicate the action identifier
     */
    public static final String ACTION_ID_LABEL = "id";

    public static final String VM_LOCATION_LABEL = "location";

    public static final String VM_DESTINATION_LABEL = "destination";

    @Override
    public Action fromJSON(JSONObject in) throws JSONConverterException {
        String id = in.get(ACTION_ID_LABEL).toString();
        if (id == null) {
            throw new JSONConverterException("The action identifier is expected on the key 'id'");
        }

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
                throw new JSONConverterException(("Unsupported type of action '" + id + "'"));
        }

        //Decorate with the events
        if (in.containsKey("hooks")) {
            JSONObject hooks = (JSONObject) in.get("hooks");
            for (String k : hooks.keySet()) {
                Action.Hook h = Action.Hook.valueOf(k);
                if (h == null) {
                    throw new JSONConverterException(("Unsupported hook type '" + k + "'"));
                }
                for (Object o : (JSONArray) hooks.get(k)) {
                    a.addEvent(h, eventFromJSON((JSONObject) o));
                }
            }
        }
        return a;
    }

    private Event eventFromJSON(JSONObject o) throws JSONConverterException {
        String id = o.get(ACTION_ID_LABEL).toString();
        if (id == null) {
            throw new JSONConverterException("The action identifier is expected on the key 'id'");
        }
        switch (id) {
            case "allocate":
                return allocateEventFromJSON(o);
            case "substitutedVM":
                return substitutedVMEventFromJSON(o);
            default:
                throw new JSONConverterException(("Unsupported type of action '" + id + "'"));
        }
    }


    @Override
    public JSONObject visit(BootVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "bootVM");
        o.put(VM_LABEL, a.getVM().toString());
        o.put("destination", a.getDestinationNode().toString());
        return o;
    }

    private BootVM bootVMFromJSON(JSONObject in) throws JSONConverterException {
        return new BootVM(requiredUUID(in, VM_LABEL),
                requiredUUID(in, "destination"),
                (int) requiredLong(in, START_LABEL),
                (int) requiredLong(in, END_LABEL));
    }

    @Override
    public JSONObject visit(ShutdownVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "shutdownVM");
        o.put(VM_LABEL, a.getVM().toString());
        o.put(VM_LOCATION_LABEL, a.getNode().toString());
        return o;
    }

    private ShutdownVM shutdownVMFromJSON(JSONObject in) throws JSONConverterException {
        return new ShutdownVM(requiredUUID(in, VM_LABEL),
                requiredUUID(in, VM_LOCATION_LABEL),
                (int) requiredLong(in, START_LABEL),
                (int) requiredLong(in, END_LABEL));
    }

    @Override
    public JSONObject visit(ShutdownNode a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "shutdownNode");
        o.put("node", a.getNode().toString());
        return o;
    }

    private ShutdownNode shutdownNodeFromJSON(JSONObject in) throws JSONConverterException {
        return new ShutdownNode(requiredUUID(in, "node"),
                (int) requiredLong(in, START_LABEL),
                (int) requiredLong(in, END_LABEL));
    }

    @Override
    public JSONObject visit(BootNode a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "bootNode");
        o.put("node", a.getNode().toString());
        return o;
    }

    private BootNode bootNodeFromJSON(JSONObject in) throws JSONConverterException {
        return new BootNode(requiredUUID(in, "node"),
                (int) requiredLong(in, START_LABEL),
                (int) requiredLong(in, END_LABEL));
    }

    @Override
    public JSONObject visit(MigrateVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "migrateVM");
        o.put(VM_LABEL, a.getVM().toString());
        o.put(VM_DESTINATION_LABEL, a.getDestinationNode().toString());
        o.put(VM_LOCATION_LABEL, a.getSourceNode().toString());
        return o;
    }


    private MigrateVM migrateVMFromJSON(JSONObject in) throws JSONConverterException {
        return new MigrateVM(requiredUUID(in, VM_LABEL),
                requiredUUID(in, VM_LOCATION_LABEL),
                requiredUUID(in, VM_DESTINATION_LABEL),
                (int) requiredLong(in, START_LABEL),
                (int) requiredLong(in, END_LABEL));
    }

    @Override
    public JSONObject visit(SuspendVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "suspendVM");
        o.put(VM_LABEL, a.getVM().toString());
        o.put(VM_DESTINATION_LABEL, a.getDestinationNode().toString());
        o.put(VM_LOCATION_LABEL, a.getSourceNode().toString());
        return o;
    }

    private SuspendVM suspendVMFromJSON(JSONObject in) throws JSONConverterException {
        return new SuspendVM(requiredUUID(in, VM_LABEL),
                requiredUUID(in, VM_LOCATION_LABEL),
                requiredUUID(in, VM_DESTINATION_LABEL),
                (int) requiredLong(in, START_LABEL),
                (int) requiredLong(in, END_LABEL));
    }

    @Override
    public JSONObject visit(ResumeVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "resumeVM");
        o.put(VM_LABEL, a.getVM().toString());
        o.put(VM_DESTINATION_LABEL, a.getDestinationNode().toString());
        o.put(VM_LOCATION_LABEL, a.getSourceNode().toString());
        return o;
    }

    private ResumeVM resumeVMFromJSON(JSONObject in) throws JSONConverterException {
        return new ResumeVM(requiredUUID(in, VM_LABEL),
                requiredUUID(in, VM_LOCATION_LABEL),
                requiredUUID(in, VM_DESTINATION_LABEL),
                (int) requiredLong(in, START_LABEL),
                (int) requiredLong(in, END_LABEL));
    }

    @Override
    public JSONObject visit(KillVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "killVM");
        o.put(VM_LABEL, a.getVM().toString());
        o.put(VM_LOCATION_LABEL, a.getNode().toString());
        return o;
    }

    private KillVM killVMFromJSON(JSONObject in) throws JSONConverterException {
        return new KillVM(requiredUUID(in, VM_LABEL),
                requiredUUID(in, VM_LOCATION_LABEL),
                (int) requiredLong(in, START_LABEL),
                (int) requiredLong(in, END_LABEL));

    }

    @Override
    public JSONObject visit(ForgeVM a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "forgeVM");
        o.put(VM_LABEL, a.getVM().toString());
        return o;

    }

    private ForgeVM forgeVMFromJSON(JSONObject in) throws JSONConverterException {
        return new ForgeVM(requiredUUID(in, VM_LABEL),
                (int) requiredLong(in, START_LABEL),
                (int) requiredLong(in, END_LABEL));
    }

    @Override
    public JSONObject visit(Allocate a) {
        JSONObject o = makeActionSkeleton(a);
        o.put(ACTION_ID_LABEL, "allocate");
        o.put(VM_LABEL, a.getVM().toString());
        o.put("rc", a.getResourceId());
        o.put("qty", a.getAmount());
        o.put(VM_LOCATION_LABEL, a.getHost().toString());
        return o;
    }

    private Allocate allocateFromJSON(JSONObject in) throws JSONConverterException {
        return new Allocate(requiredUUID(in, VM_LABEL),
                requiredUUID(in, VM_LOCATION_LABEL),
                requiredString(in, "rc"),
                (int) requiredLong(in, "qty"),
                (int) requiredLong(in, START_LABEL),
                (int) requiredLong(in, END_LABEL));
    }

    @Override
    public Object visit(AllocateEvent a) {
        JSONObject o = new JSONObject();
        o.put(ACTION_ID_LABEL, "allocate");
        o.put("rc", a.getResourceId());
        o.put(VM_LABEL, a.getVM().toString());
        o.put("qty", a.getAmount());
        return o;
    }

    private AllocateEvent allocateEventFromJSON(JSONObject o) throws JSONConverterException {
        return new AllocateEvent(requiredUUID(o, VM_LABEL),
                requiredString(o, "rc"),
                (int) requiredLong(o, "qty"));
    }

    @Override
    public Object visit(SubstitutedVMEvent a) {
        JSONObject o = new JSONObject();
        o.put(ACTION_ID_LABEL, "substitutedVM");
        o.put(VM_LABEL, a.getVM().toString());
        o.put("newUUID", a.getNewUUID().toString());
        return o;
    }

    private SubstitutedVMEvent substitutedVMEventFromJSON(JSONObject o) throws JSONConverterException {
        return new SubstitutedVMEvent(requiredUUID(o, VM_LABEL),
                requiredUUID(o, "newUUID"));
    }

    @Override
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
        o.put("hooks", hooks);
        return o;
    }

    private JSONObject toJSON(Event e) {
        return (JSONObject) e.visit(this);
    }

    @Override
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

    @Override
    public JSONArray toJSON(Collection<Action> e) throws JSONConverterException {
        JSONArray arr = new JSONArray();
        for (Action cstr : e) {
            arr.add(toJSON(cstr));
        }
        return arr;
    }

    @Override
    public List<Action> listFromJSON(File path) throws IOException, JSONConverterException {
        try (BufferedReader in = new BufferedReader(new FileReader(path))) {
            return listFromJSON(in);
        }

    }

    @Override
    public List<Action> listFromJSON(String buf) throws IOException, JSONConverterException {
        try (StringReader in = new StringReader(buf)) {
            return listFromJSON(in);
        }
    }

    @Override
    public List<Action> listFromJSON(Reader r) throws IOException, JSONConverterException {
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
    public String toJSONString(Collection<Action> o) throws JSONConverterException {
        return toJSON(o).toJSONString();
    }

    @Override
    public void toJSON(Collection<Action> e, Appendable w) throws JSONConverterException, IOException {
        toJSON(e).writeJSONString(w);
    }

    @Override
    public void toJSON(Collection<Action> e, File path) throws JSONConverterException, IOException {
        try (FileWriter out = new FileWriter(path)) {
            toJSON(e, out);
        }
    }
}
