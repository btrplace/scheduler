package btrplace.instance.json;

import btrplace.instance.Configuration;
import btrplace.instance.DefaultConfiguration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.UUID;

/**
 * Class to serialize and un-serialize {@link Configuration}.
 *
 * @author Fabien Hermenier
 */
public class JSONConfiguration {

    private JSONParser parser = new JSONParser();

    public JSONObject toJSON(Configuration c) {
        JSONObject o = new JSONObject();
        o.put("offlineNodes", Utils.toJSON(c.getOfflineNodes()));
        o.put("waitingVMs", Utils.toJSON(c.getWaitingVMs()));

        JSONObject ons = new JSONObject();
        for (UUID n : c.getOnlineNodes()) {
            JSONObject w = new JSONObject();
            w.put("runningVMs", Utils.toJSON(c.getRunningVMs(n)));
            w.put("sleepingVMs", Utils.toJSON(c.getRunningVMs(n)));
            ons.put(n.toString(), w);
        }
        o.put("onlineNodes", ons);
        return o;
    }

    public Configuration fromJSON(String in) {
        StringReader r = null;
        try {
            r = new StringReader(in);
            return fromJSON(r);
        } catch (IOException e) {
            return null;
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    public Configuration fromJSON(Reader in) throws IOException {
        Configuration c = new DefaultConfiguration();
        try {
            JSONObject o = (JSONObject) parser.parse(in);
            if (!o.containsKey("offlineNodes") || !o.containsKey("waitingVMs")
                    || !o.containsKey("onlineNodes")) {
                return null;
            }
            for (UUID u : Utils.fromJSON((JSONArray) o.get("offlineNodes"))) {
                c.addOfflineNode(u);
            }
            for (UUID u : Utils.fromJSON((JSONArray) o.get("waitingVMs"))) {
                c.addWaitingVM(u);
            }
            JSONObject ons = (JSONObject) o.get("onlineNodes");
            for (Object k : ons.keySet()) {
                UUID u = UUID.fromString((String) k);
                JSONObject on = (JSONObject) ons.get(k);
                if (!on.containsKey("runningVMs") || !on.containsKey("sleepingVMs")) {
                    return null;
                }
                c.addOnlineNode(u);
                for (UUID vmId : Utils.fromJSON((JSONArray) on.get("runningVMs"))) {
                    c.setVMRunOn(vmId, u);
                }
                for (UUID vmId : Utils.fromJSON((JSONArray) on.get("sleepingVMs"))) {
                    c.setVMRunOn(vmId, u);
                }
            }


        } catch (ParseException ex) {
            return null;
        }
        return c;
    }
}
