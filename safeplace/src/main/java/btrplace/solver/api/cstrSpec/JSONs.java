package btrplace.solver.api.cstrSpec;

import btrplace.json.JSONConverterException;
import btrplace.json.model.constraint.ConstraintsConverter;
import btrplace.model.Element;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;

import java.io.IOException;
import java.util.*;

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
            if (ite.hasNext()) {
                str.append(unMarshal(ite.next()));
                while (ite.hasNext()) {
                    str.append(", ");
                    str.append(unMarshal(ite.next()));
                }
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

    public static SatConstraint unMarshalConstraint(ReconfigurationPlan p, Constraint cstr, List<Constant> in) throws JSONConverterException, IOException {
        ConstraintsConverter conv = ConstraintsConverter.newBundle();
        String marshal = cstr.getMarshal();
        List<UserVar> vars = cstr.getParameters();
        Map<String, Object> ps = new HashMap<>();
        for (int i = 0; i < vars.size(); i++) {
            ps.put(vars.get(i).label(), in.get(i).eval(null));
        }
        conv.setModel(p.getOrigin());
        return (SatConstraint) conv.fromJSON(unMarshal(marshal, ps));
    }
}
