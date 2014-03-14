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
public class SignatureReducer {

    private Verifier v;

    public SignatureReducer(Verifier v) {
        this.v = v;
    }

    private boolean consistent(ReconfigurationPlan p, Constraint cstr, List<Constant> in) throws Exception {
        TestCase tc = new TestCase(v, cstr, p, in, true);
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

    public List<Constant> reduce(ReconfigurationPlan p, Constraint cstr, List<Constant> in) throws Exception {
        List<Constant> cpy = deepCopy(in);
        if (consistent(p, cstr, cpy)) {
            return cpy;
        }
        for (int i = 0; i < cpy.size(); i++) {
            reduceArg(p, cstr, cpy, i);
        }
        return cpy;
    }

    private void reduceArg(ReconfigurationPlan p, Constraint cstr, List<Constant> in, int i) throws Exception {
        Constant c = in.get(i);
        if (c.type() instanceof SetType) {
            List l = (List) c.eval(null);
            in.set(i, new Constant(l, c.type()));
            for (int j = 0; j < l.size(); j++) {
                if (reduceSetTo(p, cstr, in, l, j)) {
                    j--;
                }
            }
        }
    }

    private boolean reduceSetTo(ReconfigurationPlan p, Constraint cstr, List<Constant> in, List col, int i) throws Exception {
        if (col.get(i) instanceof Collection) {
            if (failWithout(p, cstr, in, col, i)) {
                return true;
            }
            List l = (List) col.get(i);
            col.set(i, l);
            for (int j = 0; j < l.size(); j++) {
                if (reduceSetTo(p, cstr, in, l, j)) {
                    j--;
                }
            }
            return false;
        } else {
            return failWithout(p, cstr, in, col, i);
        }
    }

    private boolean failWithout(ReconfigurationPlan p, Constraint cstr, List<Constant> in, List col, int i) throws Exception {
        Object o = col.remove(i);

        if (consistent(p, cstr, in)) { //Not the same error. Component needed
            col.add(i, o);
        }
        return true;
    }
}