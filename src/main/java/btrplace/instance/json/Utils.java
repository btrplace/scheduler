package btrplace.instance.json;

import org.json.simple.JSONArray;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tools to help at converting object to JSON.
 *
 * @author Fabien Hermenier
 */
public class Utils {

    private Utils() {
    }

    public static Set<UUID> fromJSON(JSONArray a) {
        Set<UUID> s = new HashSet<UUID>(a.size());
        for (Object o : a) {
            s.add(UUID.fromString((String) o));
        }
        return s;
    }

    public static JSONArray toJSON(Set<UUID> s) {
        JSONArray a = new JSONArray();
        for (UUID u : s) {
            a.add(u.toString());
        }
        return a;
    }
}
