package btrplace.instance.json;

import org.json.simple.JSONArray;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: fhermeni
 * Date: 09/11/12
 * Time: 23:40
 * To change this template use File | Settings | File Templates.
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
}
