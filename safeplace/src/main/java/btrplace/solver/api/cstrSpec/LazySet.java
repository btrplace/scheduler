package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 *         TODO: multiple variables
 */
public class LazySet {

    private Proposition p;

    private UserVar v;
    private Term t;

    public LazySet(Term t, UserVar v, Proposition p) {
        this.p = p;
        this.t = t;
        this.v = v;
    }

    public Set expand(SpecModel mo) {
        Set res = new HashSet();
        /*for (Object v : (Collection) vars.get(0).getBackend().eval(mo)) {
            vars.get(0).set(v);
            Object o = t.eval(mo);
            res.add(o);
            vars.get(0).unset();
        } */
        return res;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("{").append(t).append(". ");
        b.append(v.pretty());
        if (!p.equals(Proposition.True)) {

            b.append(" , ").append(p);
        }
        return b.append('}').toString();
    }
}
