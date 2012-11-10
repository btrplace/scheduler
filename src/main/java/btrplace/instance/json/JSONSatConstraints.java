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
        ps.put("vms", Utils.toJSON(s.getInvolvedVMs()));
        o.put("params", ps);

        return o;
    }

    public Spread spreadFromJSON(JSONObject o) {
        if (!"spread".equals(o.get("id")) || o.get("params") == null) {
            return null;
        }
        JSONObject params = (JSONObject) o.get("params");
        return new Spread(Utils.fromJSON((JSONArray) params.get("vms")));
    }

    public JSONObject banToJSON(Ban b) {
        JSONObject o = new JSONObject();
        o.put("id", "ban");
        JSONObject ps = new JSONObject();
        ps.put("vms", Utils.toJSON(b.getInvolvedVMs()));
        ps.put("nodes", Utils.toJSON(b.getInvolvedNodes()));
        o.put("params", ps);
        return o;
    }

    public Ban banFromJSON(JSONObject o) {
        if (!"ban".equals(o.get("id")) || o.get("params") == null) {
            return null;
        }
        JSONObject params = (JSONObject) o.get("params");
        return new Ban(Utils.fromJSON((JSONArray) params.get("vms")), Utils.fromJSON((JSONArray) params.get("nodes")));
    }

    public JSONObject fenceToJSON(Fence f) {
        JSONObject o = new JSONObject();
        o.put("id", "fence");

        JSONObject ps = new JSONObject();
        ps.put("vms", Utils.toJSON(f.getInvolvedVMs()));
        ps.put("nodes", Utils.toJSON(f.getInvolvedNodes()));
        o.put("params", ps);
        return o;
    }

    public Fence fenceFromJSON(JSONObject o) {
        if (!"fence".equals(o.get("id")) || o.get("params") == null) {
            return null;
        }
        JSONObject params = (JSONObject) o.get("params");

        return new Fence(Utils.fromJSON((JSONArray) params.get("vms")), Utils.fromJSON((JSONArray) params.get("nodes")));
    }


    public JSONObject destroyedToJSON(Destroyed d) {
        JSONObject o = new JSONObject();
        o.put("id", "destroyed");
        JSONObject ps = new JSONObject();
        ps.put("vms", Utils.toJSON(d.getInvolvedVMs()));
        o.put("params", ps);
        return o;
    }

    public Destroyed destroyedFromJSON(JSONObject o) {
        if (!"destroyed".equals(o.get("id")) || o.get("params") == null) {
            return null;
        }
        JSONObject params = (JSONObject) o.get("params");

        return new Destroyed(Utils.fromJSON((JSONArray) params.get("vms")));
    }


    public JSONObject runningToJSON(Running r) {
        JSONObject o = new JSONObject();
        o.put("id", "running");

        JSONObject ps = new JSONObject();
        ps.put("vms", Utils.toJSON(r.getInvolvedVMs()));
        o.put("params", ps);
        return o;
    }

    public Running runningFromJSON(JSONObject o) {
        if (!"running".equals(o.get("id")) || o.get("params") == null) {
            return null;
        }
        JSONObject params = (JSONObject) o.get("params");

        return new Running(Utils.fromJSON((JSONArray) params.get("vms")));
    }

    public JSONObject sleepingToJSON(Sleeping s) {
        JSONObject o = new JSONObject();
        o.put("id", "sleeping");

        JSONObject ps = new JSONObject();
        ps.put("vms", Utils.toJSON(s.getInvolvedVMs()));
        o.put("params", ps);
        return o;
    }

    public Sleeping sleepingFromJSON(JSONObject o) {
        if (!"sleeping".equals(o.get("id")) || o.get("params") == null) {
            return null;
        }
        JSONObject params = (JSONObject) o.get("params");

        return new Sleeping(Utils.fromJSON((JSONArray) params.get("vms")));
    }

    public JSONObject waitingToJSON(Waiting w) {
        JSONObject o = new JSONObject();

        o.put("id", "waiting");

        JSONObject ps = new JSONObject();
        ps.put("vms", Utils.toJSON(w.getInvolvedVMs()));
        o.put("params", ps);
        return o;
    }

    public Waiting waitingFromJSON(JSONObject o) {
        if (!"waiting".equals(o.get("id")) || o.get("params") == null) {
            return null;
        }
        JSONObject params = (JSONObject) o.get("params");

        return new Waiting(Utils.fromJSON((JSONArray) params.get("vms")));
    }

    public JSONObject offlineToJSON(Offline off) {
        JSONObject o = new JSONObject();
        o.put("id", "offline");

        JSONObject ps = new JSONObject();
        ps.put("nodes", Utils.toJSON(off.getInvolvedNodes()));
        o.put("params", ps);
        return o;
    }

    public Offline offlineFromJSON(JSONObject o) {
        if (!"offline".equals(o.get("id")) || o.get("params") == null) {
            return null;
        }
        JSONObject params = (JSONObject) o.get("params");

        return new Offline(Utils.fromJSON((JSONArray) params.get("nodes")));
    }


    public JSONObject onlineToJSON(Online on) {
        JSONObject o = new JSONObject();
        o.put("id", "online");

        JSONObject ps = new JSONObject();
        ps.put("nodes", Utils.toJSON(on.getInvolvedNodes()));
        o.put("params", ps);
        return o;
    }

    public Online onlineFromJSON(JSONObject o) {
        if (!"online".equals(o.get("id")) || o.get("params") == null) {
            return null;
        }
        JSONObject params = (JSONObject) o.get("params");

        return new Online(Utils.fromJSON((JSONArray) params.get("nodes")));
    }

    public JSONObject preserveToJSON(Preserve p) {
        JSONObject o = new JSONObject();
        o.put("id", "preserve");

        JSONObject ps = new JSONObject();
        ps.put("vms", Utils.toJSON(p.getInvolvedVMs()));
        ps.put("rc", p.getResource());
        ps.put("amount", p.getAmount());
        o.put("params", ps);
        return o;
    }

    public Preserve preserveFromJSON(JSONObject o) {
        if (!"preserve".equals(o.get("id")) || o.get("params") == null) {
            return null;
        }
        JSONObject params = (JSONObject) o.get("params");

        return new Preserve(Utils.fromJSON((JSONArray) params.get("vms")), (String) params.get("rc"), Integer.parseInt((String) params.get("amount")));
    }

    public JSONObject rootToJSON(Root s) {
        JSONObject o = new JSONObject();
        o.put("id", "root");

        JSONObject ps = new JSONObject();
        ps.put("vms", Utils.toJSON(s.getInvolvedVMs()));
        o.put("params", ps);
        return o;
    }

    public Root rootFromJSON(JSONObject o) {
        if (!"root".equals(o.get("id")) || o.get("params") == null) {
            return null;
        }
        JSONObject params = (JSONObject) o.get("params");

        return new Root(Utils.fromJSON((JSONArray) params.get("vms")));
    }

    public JSONObject splitToJSON(Split s) {
        JSONObject o = new JSONObject();
        o.put("id", "split");

        JSONObject ps = new JSONObject();
        JSONArray arr = new JSONArray();
        for (Set<UUID> set : s.getSets()) {
            arr.add(Utils.toJSON(set));
        }
        ps.put("vms", arr);
        o.put("params", ps);
        return o;
    }

    public Split splitFromJSON(JSONObject o) {
        if (!"split".equals(o.get("id")) || o.get("params") == null) {
            return null;
        }
        JSONObject params = (JSONObject) o.get("params");

        Set<Set<UUID>> ss = new HashSet<Set<UUID>>();
        for (Object ob : (JSONArray) params.get("vms")) {
            ss.add(Utils.fromJSON((JSONArray) ob));
        }
        return new Split(ss);
    }

    public JSONObject splitAmongToJSON(SplitAmong s) {
        JSONObject o = new JSONObject();
        o.put("id", "splitAmong");
        JSONArray arr = new JSONArray();

        JSONObject ps = new JSONObject();
        for (Set<UUID> set : s.getGroupsOfVMs()) {
            arr.add(Utils.toJSON(set));
        }
        ps.put("vms", arr);

        arr = new JSONArray();
        for (Set<UUID> set : s.getGroupsOfNodes()) {
            arr.add(Utils.toJSON(set));
        }
        ps.put("nodes", arr);
        o.put("params", ps);
        return o;
    }

    public SplitAmong splitAmongFromJSON(JSONObject o) {
        if (!"splitAmong".equals(o.get("id")) || o.get("params") == null) {
            return null;
        }
        JSONObject params = (JSONObject) o.get("params");

        Set<Set<UUID>> ss = new HashSet<Set<UUID>>();
        for (Object ob : (JSONArray) params.get("vms")) {
            ss.add(Utils.fromJSON((JSONArray) ob));
        }

        Set<Set<UUID>> ss2 = new HashSet<Set<UUID>>();
        for (Object ob : (JSONArray) params.get("nodes")) {
            ss2.add(Utils.fromJSON((JSONArray) ob));
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

            if ("ban".equals(id)) {
                return banFromJSON(o);
            } else if ("destroyed".equals(id)) {
                return destroyedFromJSON(o);
            } else if ("fence".equals(id)) {
                return fenceFromJSON(o);
            } else if ("offline".equals(id)) {
                return offlineFromJSON(o);
            } else if ("online".equals(id)) {
                return onlineFromJSON(o);
            } else if ("preserve".equals(id)) {
                return preserveFromJSON(o);
            } else if ("root".equals(id)) {
                return rootFromJSON(o);
            } else if ("running".equals(id)) {
                return runningFromJSON(o);
            } else if ("sleeping".equals(id)) {
                return sleepingFromJSON(o);
            } else if ("split".equals(id)) {
                return splitFromJSON(o);
            } else if ("splitAmong".equals(id)) {
                return splitAmongFromJSON(o);
            } else if ("spread".equals(id)) {
                return spreadFromJSON(o);
            } else if ("waiting".equals(id)) {
                return waitingFromJSON(o);
            } else { //Unknown constraint
                return null;
            }

        } catch (ParseException ex) {
            return null;
        }
    }
}
