package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Build a set of elements from a variable, a proposition to test which of the values have to be inserted,
 * and a term to perform transformations on the variable.
 *
 * @author Fabien Hermenier
 *         TODO: multiple variables
 */
public class ListBuilder {

    private Proposition p;

    private UserVar v;
    private Term t;

    public ListBuilder(Term t, UserVar v, Proposition p) {
        this.p = p;
        this.t = t;
        this.v = v;
    }

    public List expand(SpecModel mo) {
        List res = new ArrayList();
        List<Constant> domain = v.domain(mo);
        for (Constant c : domain) {
            v.set(mo, c.eval(mo));
            Boolean ok = p.eval(mo);
            if (ok) {
                res.add(t.eval(mo));
            }
        }
        v.unset();
        return res;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("[").append(t).append(". ");
        b.append(v.pretty());
        if (!p.equals(Proposition.True)) {

            b.append(" , ").append(p);
        }
        return b.append(']').toString();
    }
}
