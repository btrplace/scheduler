package btrplace.solver.api.cstrSpec.reducer;

import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.Verifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reduce a constraint signature to the minimum possible.
 * <p/>
 * In practice the sets are reduced one by one by removing values one by one.
 * A value will stay in the set if its removal lead to a different error.
 *
 * @author Fabien Hermenier
 */
public class SignatureReducer implements Reducer {

    private boolean consistent(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in, boolean d) throws Exception {
        TestCase tc = new TestCase(v, cstr, p, in, d);
        return tc.succeed();
    }

    private List<Constant> deepCopy(List<Constant> in) {
        List<Constant> cpy = new ArrayList<>(in.size());
        for (Constant c : in) {
            Type t = c.type();
            Object v = c.eval(null);
            Object o = v; //Assume immutable if not a collection
            if (v instanceof Collection) {
                o = toList((Collection) v);
                //Deep transformation into lists
            }
            cpy.add(new Constant(o, t));
        }
        return cpy;
    }

    private List toList(Collection v) {
        List l = new ArrayList(v.size());
        for (Object o : v) {
            if (o instanceof Collection) {
                l.add(toList((Collection) o));
            } else {
                //Assume immutable
                l.add(o);
            }
        }
        return l;
    }

    public TestCase reduce(TestCase tc) throws Exception {
        ReconfigurationPlan p = tc.getPlan();
        Constraint cstr = tc.getConstraint();
        List<Constant> in = tc.getArguments();
        List<Constant> cpy = deepCopy(in);
        if (consistent(tc.getVerifier(), p, cstr, cpy, tc.isDiscrete())) {
            return new TestCase(tc.getVerifier(), cstr, p, cpy, tc.isDiscrete());
        }
        for (int i = 0; i < cpy.size(); i++) {
            reduceArg(tc.getVerifier(), p, cstr, cpy, tc.isDiscrete(), i);
        }
        //return cpy;
        return new TestCase(tc.getVerifier(), cstr, p, cpy, tc.isDiscrete());
    }

    private void reduceArg(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in, boolean d, int i) throws Exception {
        Constant c = in.get(i);
        if (c.type() instanceof SetType) {
            List l = (List) c.eval(null);
            in.set(i, new Constant(l, c.type()));
            for (int j = 0; j < l.size(); j++) {
                if (reduceSetTo(v, p, cstr, in, d, l, j)) {
                    j--;
                }
            }
        }
    }

    private boolean reduceSetTo(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in, boolean d, List col, int i) throws Exception {
        if (col.get(i) instanceof Collection) {
            if (failWithout(v, p, cstr, in, d, col, i)) {
                return true;
            }
            List l = (List) col.get(i);
            col.set(i, l);
            for (int j = 0; j < l.size(); j++) {
                if (reduceSetTo(v, p, cstr, in, d, l, j)) {
                    j--;
                }
            }
            return false;
        } else {
            return failWithout(v, p, cstr, in, d, col, i);
        }
    }

    private boolean failWithout(Verifier v, ReconfigurationPlan p, Constraint cstr, List<Constant> in, boolean d, List col, int i) throws Exception {
        Object o = col.remove(i);

        if (consistent(v, p, cstr, in, d)) { //Not the same error. Component needed
            col.add(i, o);
        }
        return true;
    }
}