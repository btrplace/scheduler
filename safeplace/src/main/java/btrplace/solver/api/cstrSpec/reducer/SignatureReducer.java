package btrplace.solver.api.cstrSpec.reducer;

import btrplace.json.JSONConverterException;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.CstrSpecEvaluator;
import btrplace.solver.api.cstrSpec.JSONs;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.TestResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reduce a constraint signature to the minimum possible.
 *
 * In practice the sets are reduced one by one by removing values one by one.
 * A value will stay in the set if its removal lead to a different error.
 * @author Fabien Hermenier
 */
public class SignatureReducer {

    private ImplVerifier verif;

    private CstrSpecEvaluator cVerif;

    public SignatureReducer() {
        verif = new ImplVerifier();
        cVerif = new CstrSpecEvaluator();
    }

    private TestResult.ErrorType compare(ReconfigurationPlan p, Constraint cstr, List<Constant> in) throws JSONConverterException, IOException {
        boolean consTh = cVerif.eval(cstr, p, in);

        SatConstraint impl = JSONs.unMarshalConstraint(p, cstr, in);
        return verif.verify(new TestCase(0, p, impl, consTh), false).errorType();
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

    public List<Constant> reduce(ReconfigurationPlan p, Constraint cstr, List<Constant> in) throws IOException, JSONConverterException {
        List<Constant> cpy = deepCopy(in);
        TestResult.ErrorType t = compare(p, cstr, cpy);
        if (t == TestResult.ErrorType.succeed) {
            return cpy;
        }
        for (int i = 0; i < cpy.size(); i++) {
            reduceArg(t, p, cstr, cpy, i);
        }
        return cpy;
    }

    private void reduceArg(TestResult.ErrorType t, ReconfigurationPlan p, Constraint cstr, List<Constant> in, int i) throws IOException, JSONConverterException {
        Constant c = in.get(i);
        if (c.type() instanceof SetType) {
            List l = (List) c.eval(null);
            in.set(i, new Constant(l, c.type()));
            for (int j = 0; j < l.size(); j++) {
                if (reduceSetTo(t, p, cstr, in, l, j)) {
                    j--;
                }
            }
        }
    }

    private boolean reduceSetTo(TestResult.ErrorType t, ReconfigurationPlan p, Constraint cstr, List<Constant> in, List col, int i) throws IOException, JSONConverterException {
        if (col.get(i) instanceof Collection) {
            if (failWithout(t, p, cstr, in, col, i)) {
                return true;
            }
            List l = (List) col.get(i);
            col.set(i, l);
            for (int j = 0; j < l.size(); j++) {
                if (reduceSetTo(t, p, cstr, in, l, j)) {
                    j--;
                }
            }
            return false;
        } else {
            return failWithout(t, p, cstr, in, col, i);
        }
    }

    private boolean failWithout(TestResult.ErrorType t, ReconfigurationPlan p, Constraint cstr, List<Constant> in, List col, int i) throws IOException, JSONConverterException {
        Object o = col.remove(i);

        TestResult.ErrorType t2 = compare(p, cstr, in);
        boolean ret = t.equals(t2);
        if (!ret) { //Not the same error. Component needed
            col.add(i, o);
        }
        return ret;
    }
}