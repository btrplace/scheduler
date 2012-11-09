package btrplace.instance.json;

import btrplace.instance.constraint.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Serialize / unserialize constraints.
 *
 * @author Fabien Hermenier
 */
public class JSONSatConstraints {

    private JSONParser p;

    public JSONSatConstraints() {
        p = new JSONParser();
    }

    public JSONObject spreadToJSON(Spread s) {
        JSONObject o = new JSONObject();
        o.put("id", "spread");
        JSONObject ps = new JSONObject();
        ps.put("vms", s.getInvolvedVMs());
        o.put("params", ps);

        return o;
    }

    public Spread spreadFromJSON(JSONObject params) {
        return new Spread(Utils.fromJSON((JSONArray) params.get("vms")));
    }

    public JSONObject banToJSON(Ban b) {
        JSONObject o = new JSONObject();
        o.put("id", "ban");
        JSONObject ps = new JSONObject();
        ps.put("vms", b.getInvolvedVMs());
        ps.put("nodes", b.getInvolvedNodes());
        o.put("params", ps);
        return o;
    }

    public Ban banFromJSON(JSONObject params) {
        return new Ban(Utils.fromJSON((JSONArray) params.get("vms")), Utils.fromJSON((JSONArray) params.get("nodes")));
    }

    public JSONObject fenceToJSON(Fence f) {
        JSONObject o = new JSONObject();
        o.put("id", "fence");

        JSONObject ps = new JSONObject();
        ps.put("vms", f.getInvolvedVMs());
        ps.put("nodes", f.getInvolvedNodes());
        o.put("params", ps);
        return o;
    }

    public Fence fenceFromJSON(JSONObject params) {
        return new Fence(Utils.fromJSON((JSONArray) params.get("vms")), Utils.fromJSON((JSONArray) params.get("nodes")));
    }


    public JSONObject destroyedToJSON(Destroyed d) {
        JSONObject o = new JSONObject();
        o.put("id", "destroyed");
        JSONObject ps = new JSONObject();
        ps.put("vms", d.getInvolvedVMs());
        o.put("params", ps);
        return o;
    }

    public Destroyed destroyedFromJSON(JSONObject params) {
        return new Destroyed(Utils.fromJSON((JSONArray) params.get("vms")));
    }


    public JSONObject runningToJSON(Running r) {
        JSONObject o = new JSONObject();
        o.put("id", "running");

        JSONObject ps = new JSONObject();
        ps.put("vms", r.getInvolvedVMs());
        o.put("params", ps);
        return o;
    }

    public Running runningFromJSON(JSONObject params) {
        return new Running(Utils.fromJSON((JSONArray) params.get("vms")));
    }

    public JSONObject sleepingToJSON(Sleeping s) {
        JSONObject o = new JSONObject();
        o.put("id", "sleeping");

        JSONObject ps = new JSONObject();
        ps.put("vms", s.getInvolvedVMs());
        o.put("params", ps);
        return o;
    }

    public Sleeping sleepingFromJSON(JSONObject params) {
        return new Sleeping(Utils.fromJSON((JSONArray) params.get("vms")));
    }

    public JSONObject waitingToJSON(Waiting w) {
        JSONObject o = new JSONObject();

        o.put("id", "waiting");

        JSONObject ps = new JSONObject();
        ps.put("vms", w.getInvolvedVMs());
        o.put("params", ps);
        return o;
    }

    public Waiting waitingFromJSON(JSONObject params) {
        return new Waiting(Utils.fromJSON((JSONArray) params.get("vms")));
    }

    public JSONObject offlineToJSON(Offline off) {
        JSONObject o = new JSONObject();
        o.put("id", "offline");

        JSONObject ps = new JSONObject();
        ps.put("nodes", off.getInvolvedNodes());
        o.put("params", ps);
        return o;
    }

    public Offline offlineFromJSON(JSONObject params) {
        return new Offline(Utils.fromJSON((JSONArray) params.get("nodes")));
    }


    public JSONObject onlineToJSON(Online on) {
        JSONObject o = new JSONObject();
        o.put("id", "online");

        JSONObject ps = new JSONObject();
        ps.put("nodes", on.getInvolvedNodes());
        o.put("params", ps);
        return o;
    }

    public Online onlineFromJSON(JSONObject params) {
        return new Online(Utils.fromJSON((JSONArray) params.get("nodes")));
    }

    public JSONObject preserveToJSON(Preserve p) {
        JSONObject o = new JSONObject();
        o.put("id", "preserve");

        JSONObject ps = new JSONObject();
        ps.put("vms", p.getInvolvedVMs());
        ps.put("amount", p.getAmount());
        o.put("params", ps);
        return o;
    }

    public Preserve preserveFromJSON(JSONObject params) {
        //FIX !!
        return new Preserve(Utils.fromJSON((JSONArray) params.get("vms")), null, Integer.parseInt((String) params.get("amount")));
    }

    public JSONObject rootToJSON(Root s) {
        JSONObject o = new JSONObject();
        o.put("id", "root");

        JSONObject ps = new JSONObject();
        ps.put("vms", s.getInvolvedVMs());
        o.put("params", ps);
        return o;
    }

    public Root rootFromJSON(JSONObject params) {
        return new Root(Utils.fromJSON((JSONArray) params.get("vms")));
    }

    public JSONObject splitToJSON(Split s) {
        JSONObject o = new JSONObject();
        o.put("id", "split");

        JSONObject ps = new JSONObject();
        JSONArray arr = new JSONArray();
        for (Set<UUID> set : s.getSets()) {
            arr.add(set);
        }
        ps.put("vms", arr);
        o.put("params", ps);
        return o;
    }

    public Split splitFromJSON(JSONObject params) {
        Set<Set<UUID>> ss = new HashSet<Set<UUID>>();
        for (Object o : (JSONArray) params.get("vms")) {
            ss.add(Utils.fromJSON((JSONArray) o));
        }
        return new Split(ss);
    }

    public JSONObject splitAmongToJSON(SplitAmong s) {
        JSONObject o = new JSONObject();
        o.put("id", "splitAmong");
        JSONArray arr = new JSONArray();

        JSONObject ps = new JSONObject();
        for (Set<UUID> set : s.getGroupsOfVMs()) {
            arr.add(set);
        }
        ps.put("vms", arr);

        arr = new JSONArray();
        for (Set<UUID> set : s.getGroupsOfNodes()) {
            arr.add(set);
        }
        ps.put("nodes", arr);
        o.put("params", ps);
        return o;
    }

    public SplitAmong splitAmongFromJSON(JSONObject params) {
        Set<Set<UUID>> ss = new HashSet<Set<UUID>>();
        for (Object o : (JSONArray) params.get("vms")) {
            ss.add(Utils.fromJSON((JSONArray) o));
        }

        Set<Set<UUID>> ss2 = new HashSet<Set<UUID>>();
        for (Object o : (JSONArray) params.get("nodes")) {
            ss2.add(Utils.fromJSON((JSONArray) o));
        }

        return new SplitAmong(ss, ss2);
    }

    public SatConstraint fromJSON(Reader in) throws IOException {
        try {
            JSONObject o = (JSONObject) p.parse(in);

            if (!o.containsKey("id") || !o.containsKey("params")) {
                return null;
            }
            String id = (String) o.get("id");
            JSONObject params = (JSONObject) o.get("params");

            if ("ban".equals(id)) {
                return banFromJSON(params);
            } else if ("destroyed".equals(id)) {
                return destroyedFromJSON(params);
            } else if ("fence".equals(id)) {
                return fenceFromJSON(params);
            } else if ("offline".equals(id)) {
                return offlineFromJSON(params);
            } else if ("online".equals(id)) {
                return onlineFromJSON(params);
            } else if ("preserve".equals(id)) {
                return preserveFromJSON(params);
            } else if ("root".equals(id)) {
                return rootFromJSON(params);
            } else if ("running".equals(id)) {
                return runningFromJSON(params);
            } else if ("sleeping".equals(id)) {
                return sleepingFromJSON(params);
            } else if ("split".equals(id)) {
                return splitFromJSON(params);
            } else if ("splitAmong".equals(id)) {
                return splitAmongFromJSON(params);
            } else if ("spread".equals(id)) {
                return spreadFromJSON(params);
            } else if ("waiting".equals(id)) {
                return waitingFromJSON(params);
            } else { //Unknown constraint
                return null;
            }

        } catch (ParseException ex) {
            return null;
        }

    }
}
