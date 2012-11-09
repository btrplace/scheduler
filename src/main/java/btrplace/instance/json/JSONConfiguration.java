package btrplace.instance.json;

import btrplace.instance.Configuration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.UUID;

/**
 * Class to serialize and un-serialize {@link Configuration}.
 * @author Fabien Hermenier
 */
public class JSONConfiguration {

    private JSONParser parser = new JSONParser();

    public String toJSON(Configuration c) {
        JSONObject o = new JSONObject();
        o.put("offlineNodes",c.getOfflineNodes());
        o.put("waitingVMs",c.getWaitingVMs());

        JSONObject ons = new JSONObject();
        for (UUID n : c.getOnlineNodes()) {
            JSONObject w = new JSONObject();
            w.put("runningVMs", c.getRunningVMs(n));
            w.put("sleepingVMs", c.getRunningVMs(n));
            ons.put(n, w);
        }
        o.put("onlineNodes",ons);
        return o.toJSONString();
    }

    public Configuration fromJSON(String json) throws ParseException  {
        Object o = parser.parse(json);
        return null;
    }
}
