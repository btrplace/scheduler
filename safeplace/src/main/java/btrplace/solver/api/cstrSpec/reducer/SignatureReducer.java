package btrplace.solver.api.cstrSpec.reducer;

import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.ConstraintVerifier;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.verification.ImplVerifier;
import btrplace.solver.api.cstrSpec.verification.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reduce a constraint signature to the possible.
 * In practice we reduce the size of the sets
 *
 * @author Fabien Hermenier
 */
public class SignatureReducer implements TestCaseReducer {

    private ImplVerifier verif;

    private ConstraintVerifier cVerif;


    public SignatureReducer() {
        verif = new ImplVerifier();
        cVerif = new ConstraintVerifier();
    }

    @Override
    public List<TestCase> reduce(TestCase tc, Constraint cstr, List<Constant> in) {
        List<TestCase> res = new ArrayList<>();
        for (int i = 0; i < in.size(); i++) {
            reduceArg(tc, cstr, in, i, res);
        }
        return res;
    }

    private void reduceArg(TestCase tc, Constraint cstr, List<Constant> in, int i, List<TestCase> res) {
        Constant c = in.get(i);
        if (c.type() instanceof SetType) {
            Collection col = (Collection) c.eval(null);
            List l = new ArrayList(col);
            in.set(i, new Constant(l, c.type()));
            for (int j = 0; j < l.size(); j++) {
//                for (Object o : col) {
                if (reduceSetTo(tc, cstr, in, l, j, res)) {
                    j--;
                }
//                }

            }
        }
    }

    private boolean reduceSetTo(TestCase tc, Constraint cstr, List<Constant> in, List col, int i, List<TestCase> res) {
        if (col.get(i) instanceof Collection) {
            if (failWithout(tc, cstr, in, col, i)) {
                return true;
            }
            List l = new ArrayList((Collection) col.get(i));
            col.set(i, l);
            for (int j = 0; j < l.size(); j++) {
                if (reduceSetTo(tc, cstr, in, l, j, res)) {
                    j--;
                }
            }
            return false;
        } else {
            return failWithout(tc, cstr, in, col, i);
        }
    }

    private boolean failWithout(TestCase tc, Constraint cstr, List<Constant> in, List col, int i) {
        //System.out.println("Remove '" + col.get(i) + "' from " + col);
        Object o = col.remove(i);
        //System.out.println("params: " + in);
        boolean ret = System.currentTimeMillis() % 2 == 0;
        if (!ret) {
            col.add(i, o);
            //System.out.println("KO Undo: " + in);
        } /*else {
            System.out.println("OK: " + in);
        }   */
        return ret;
    }
}