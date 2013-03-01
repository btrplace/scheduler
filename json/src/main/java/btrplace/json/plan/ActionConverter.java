package btrplace.json.plan;

import btrplace.json.JSONConverter;
import btrplace.json.JSONConverterException;
import btrplace.plan.Action;
import btrplace.plan.event.*;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.Collection;

/**
 * JSON converter for {@link Action}.
 *
 * @author Fabien Hermenier
 */
public class ActionConverter implements JSONConverter<Action>, ActionVisitor {


    @Override
    public Action fromJSON(JSONObject in) throws JSONConverterException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject toJSON(Action a) throws JSONConverterException {
        return (JSONObject) a.visit(this);
    }

    public JSONArray toJSON(Collection<Action> actions) throws JSONConverterException {
        JSONArray arr = new JSONArray();
        for (Action a : actions) {
            arr.add(toJSON(a));
        }
        return arr;
    }

    @Override
    public JSONObject visit(Allocate a) {
        JSONObject o = makeSkeleton(a);
        o.put("vm", a.getVM());
        o.put("rc", a.getResourceId());
        o.put("qty", a.getAmount());
        o.put("location", a.getHost().toString());
        return o;
    }

    @Override
    public JSONObject visit(BootNode a) {
        JSONObject o = makeSkeleton(a);
        o.put("id", "bootNode");
        o.put("node", a.getNode().toString());
        return o;
    }

    @Override
    public JSONObject visit(BootVM a) {
        JSONObject o = makeSkeleton(a);
        o.put("id", "bootVM");
        o.put("vm", a.getVM().toString());
        o.put("destination", a.getDestinationNode().toString());
        return o;
    }

    @Override
    public JSONObject visit(ForgeVM a) {
        JSONObject o = makeSkeleton(a);
        o.put("id", "forgeVM");
        o.put("vm", a.getVM().toString());
        return o;

    }

    @Override
    public JSONObject visit(KillVM a) {
        JSONObject o = makeSkeleton(a);
        o.put("id", "killVM");
        o.put("vm", a.getVM().toString());
        o.put("location", a.getNode().toString());
        return o;
    }

    @Override
    public JSONObject visit(MigrateVM a) {
        JSONObject o = makeSkeleton(a);
        o.put("id", "migrateVM");
        o.put("vm", a.getVM().toString());
        o.put("destination", a.getDestinationNode().toString());
        o.put("location", a.getSourceNode().toString());
        return o;
    }

    /**
     * Just create the JSONObject and set the start and the end attribute.
     *
     * @param a the action to convert
     * @return a skeleton JSONObject
     */
    private JSONObject makeSkeleton(Action a) {
        JSONObject o = new JSONObject();
        o.put("start", a.getStart());
        o.put("end", a.getEnd());
        return o;
    }

    @Override
    public JSONObject visit(ResumeVM a) {
        JSONObject o = makeSkeleton(a);
        o.put("id", "resumeVM");
        o.put("vm", a.getVM().toString());
        o.put("destination", a.getDestinationNode().toString());
        o.put("location", a.getSourceNode().toString());
        return o;
    }

    @Override
    public JSONObject visit(ShutdownNode a) {
        JSONObject o = makeSkeleton(a);
        o.put("id", "shutdownNode");
        o.put("node", a.getNode().toString());
        return o;
    }

    @Override
    public JSONObject visit(ShutdownVM a) {
        JSONObject o = makeSkeleton(a);
        o.put("id", "shutdownVM");
        o.put("vm", a.getVM().toString());
        o.put("location", a.getNode().toString());
        return o;
    }

    @Override
    public JSONObject visit(SuspendVM a) {
        JSONObject o = makeSkeleton(a);
        o.put("id", "suspendVM");
        o.put("vm", a.getVM().toString());
        o.put("destination", a.getDestinationNode().toString());
        o.put("location", a.getSourceNode().toString());
        return o;
    }
}
