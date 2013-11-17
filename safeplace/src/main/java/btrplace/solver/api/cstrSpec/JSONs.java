package btrplace.solver.api.cstrSpec;

import btrplace.model.Element;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class JSONs {

    public static String marshal(String json, Map<String, Object> params) {
        for (Map.Entry<String, Object> e : params.entrySet()) {
            Object v = e.getValue();
            String s = e.getValue().toString();
            if (v instanceof Element) {
                s = Integer.toString(((Element) v).id());
            } else if (v instanceof Collection) {
                StringBuilder str = new StringBuilder();
                Iterator<Object> ite = ((Collection) v).iterator();
                str.append(((Element) ite.next()).id());
                while (ite.hasNext()) {
                    str.append(", ");
                    str.append(((Element) ite.next()).id());
                }
                s = str.toString();
            }
            json = json.replaceAll("@" + e.getKey(), s);
        }
        return json;
    }
}
