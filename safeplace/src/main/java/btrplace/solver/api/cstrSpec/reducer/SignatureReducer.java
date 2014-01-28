package btrplace.solver.api.cstrSpec.reducer;

import btrplace.json.JSONConverterException;
import btrplace.json.model.constraint.ConstraintsConverter;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.CstrSpecEvaluator;
import btrplace.solver.api.cstrSpec.JSONs;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.verification.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.TestCase;
import btrplace.solver.api.cstrSpec.verification.TestResult;

import java.io.IOException;
import java.util.*;

/**
 * Reduce a constraint signature to the possible.
 * In practice we reduce the size of the sets
 *
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

        SatConstraint impl = makeImpl(p, cstr, in);
        return verif.verify(new TestCase(0, p, impl, consTh)).errorType();
    }

    private SatConstraint makeImpl(ReconfigurationPlan p, Constraint cstr, List<Constant> in) throws JSONConverterException, IOException {
        ConstraintsConverter conv = ConstraintsConverter.newBundle();
        String marshal = cstr.getMarshal();
        List<UserVar> vars = cstr.getParameters();
        Map<String, Object> ps = new HashMap<>();
        for (int i = 0; i < vars.size(); i++) {
            ps.put(vars.get(i).label(), in.get(i).eval(null));
        }
        conv.setModel(p.getOrigin());
        return (SatConstraint) conv.fromJSON(JSONs.unMarshal(marshal, ps));
    }

    public void reduce(ReconfigurationPlan p, Constraint cstr, List<Constant> in) throws IOException, JSONConverterException {
        TestResult.ErrorType t = compare(p, cstr, in);
        for (int i = 0; i < in.size(); i++) {
            reduceArg(t, p, cstr, in, i);
        }
    }

    private void reduceArg(TestResult.ErrorType t, ReconfigurationPlan p, Constraint cstr, List<Constant> in, int i) throws IOException, JSONConverterException {
        Constant c = in.get(i);
        if (c.type() instanceof SetType) {
            Collection col = (Collection) c.eval(null);
            List l = new ArrayList(col);
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
            List l = new ArrayList((Collection) col.get(i));
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