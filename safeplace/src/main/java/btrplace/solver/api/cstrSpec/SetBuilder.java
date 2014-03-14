package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Build a set of elements from a variable, a proposition to test which of the values have to be inserted,
 * and a term to perform transformations on the variable.
 *
 * @author Fabien Hermenier
 *         TODO: multiple variables
 */
public class SetBuilder<T> extends Term<Set<T>> {

    private Proposition p;

    private UserVar v;
    private Term<T> t;

    private Type type;

    public SetBuilder(Term<T> t, UserVar v, Proposition p) {
        this.p = p;
        this.t = t;
        this.v = v;
        type = new SetType(t.type());
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public Set<T> eval(SpecModel mo) {
        Set res = new HashSet();
        List<Constant> domain = v.domain(mo);
        for (Constant c : domain) {
            mo.setValue(v.label(), c.eval(mo));
            //v.set(mo, c.eval(mo));
            Boolean ok = p.eval(mo);
            if (ok) {
                res.add(t.eval(mo));
            }
        }
        //v.unset();
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
