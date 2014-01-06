package btrplace.solver.api.cstrSpec;

import btrplace.model.Element;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class JSONs {

    public static String unMarshal(String json, Map<String, Object> params) {
        for (Map.Entry<String, Object> e : params.entrySet()) {
            json = json.replaceAll("@" + e.getKey(), unMarshal(e.getValue()));
        }
        return json;
    }

    public static String unMarshal(Object v) {
        String s;
        if (v instanceof Element) {
            s = Integer.toString(((Element) v).id());
        } else if (v instanceof Collection) {
            StringBuilder str = new StringBuilder("[");
            Iterator<Object> ite = ((Collection) v).iterator();
            str.append(unMarshal(ite.next()));
            while (ite.hasNext()) {
                str.append(", ");
                str.append(unMarshal(ite.next()));
            }
            s = str.append("]").toString();
        } else if (v instanceof Boolean) {
            s = v.equals(Boolean.TRUE) ? "true" : "false";
        } else if (v instanceof String || v instanceof Number) {
            s = v.toString();
        } else {
            throw new IllegalArgumentException("No serialisation available for value '" + v + "' (" + v.getClass().getSimpleName() + ")");
        }
        return s;
    }
}
