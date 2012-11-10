package btrplace.instance.json;

import btrplace.instance.Instance;
import btrplace.instance.IntResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Class to serialize/unserialize an Instance using the JSON format.
 *
 * @author Fabien Hermenier
 */
public class JSONInstance {

    private JSONParser p;

    public JSONInstance() {
        p = new JSONParser();

    }

    public JSONObject toJSONObject(Instance i) {
        JSONArray rcs = new JSONArray();

        JSONIntResource jrc = new JSONIntResource();
        for (IntResource rc : i.getResources()) {
            rcs.add(jrc.toJSON(rc));
        }

        JSONConfiguration jcfg = new JSONConfiguration();

        JSONObject o = new JSONObject();
        o.put("configuration", jcfg.toJSON(i.getConfiguration()));
        o.put("resources", rcs);
        return o;
    }

    public String toJSON(Instance i) {
        return toJSONObject(i).toString();
    }

    public Instance fromJSON(String buf) {
        return null;
    }
}
