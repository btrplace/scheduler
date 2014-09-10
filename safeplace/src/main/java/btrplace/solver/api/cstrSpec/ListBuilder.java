package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.spec.type.ListType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
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
public class ListBuilder<T> extends Term<List<T>> {

    private Proposition p;

    private UserVar v;
    private Term<T> t;

    private Type type;

    public ListBuilder(Term<T> t, UserVar v, Proposition p) {
        this.p = p;
        this.t = t;
        this.v = v;
        type = new ListType(t.type());
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public List<T> eval(SpecModel mo) {
        List res = new ArrayList();
        List<Constant> domain = v.domain(mo);
        for (Constant c : domain) {
            //v.set(mo, c.eval(mo));
            mo.setValue(v.label(), c.eval(mo));
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
        StringBuilder b = new StringBuilder("[").append(t).append(". ");
        b.append(v.pretty());
        if (!p.equals(Proposition.True)) {

            b.append(" , ").append(p);
        }
        return b.append(']').toString();
    }
}
