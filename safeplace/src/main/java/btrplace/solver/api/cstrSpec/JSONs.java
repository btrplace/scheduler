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
            Object s = e.getValue().toString();
            if (v instanceof Element) {
                s = ((Element) v).id();
            } else if (v instanceof Collection) {
                StringBuilder str = new StringBuilder();
                for (Iterator<Object> ite = ((Collection) v).iterator(); ite.hasNext(); ) {
                    Object o = ite.next();
                    str.append(((Element) o).id());
                    if (ite.hasNext()) {
                        str.append(", ");
                    }
                }
                s = str;
            }
            json = json.replaceAll("@" + e.getKey(), s.toString());
        }
        return json;
    }
}
