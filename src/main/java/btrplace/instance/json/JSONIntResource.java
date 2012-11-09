package btrplace.instance.json;

import btrplace.instance.IntResource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Set;
import java.util.UUID;

/**
 * Serialize/Un-serialize an {@link IntResource}.
 * @author Fabien Hermenier
 */
public class JSONIntResource {

    public String toJSON(IntResource rc) {
        JSONObject o = new JSONObject();
        o.put("id", rc.identifier());
        Set<UUID> elems = rc.getDefined();
        JSONObject values = new JSONObject();
        for (UUID u : elems) {
            values.put(u, rc.get(u));
        }
        o.put("values", values);
        return o.toString();
    }

    public IntResource fromJSON(String s) {
        return null;
    }
}
