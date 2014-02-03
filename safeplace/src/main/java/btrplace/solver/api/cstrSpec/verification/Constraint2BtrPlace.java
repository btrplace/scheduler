package btrplace.solver.api.cstrSpec.verification;

import btrplace.model.constraint.SatConstraint;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Constraint2BtrPlace {

    public static SatConstraint build(Constraint cstr, List<Constant> params) throws Exception {
        return build("btrplace.model.constraint", cstr, params);
    }

    public static SatConstraint build(String pkg, Constraint cstr, List<Constant> params) throws Exception {
        String clName = cstr.id().substring(0, 1).toUpperCase() + cstr.id().substring(1);
        Class<SatConstraint> cl = (Class<SatConstraint>) Class.forName(pkg + "." + clName);
        List<Object> values = new ArrayList<>(params.size());
        for (Constant c : params) {
            values.add(c.eval(null));
        }
        for (Constructor c : cl.getConstructors()) {
            if (c.getParameterTypes().length == values.size()) {
                return (SatConstraint) c.newInstance(values.toArray());
            }
        }
        throw new IllegalArgumentException("No constructors compatible with values '" + values + "'");
    }
}
