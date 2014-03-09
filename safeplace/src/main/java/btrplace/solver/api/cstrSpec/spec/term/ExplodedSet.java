package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class ExplodedSet extends Term<Set> {

    private List<Term> terms;

    private Type t;

    public ExplodedSet(List<Term> ts, Type enclType) {
        this.terms = ts;
        t = new SetType(enclType);
    }

    @Override
    public Set eval(SpecModel mo) {
        Set s = new HashSet<>();
        for (Term t : terms) {
            s.add(t.eval(mo));
        }
        return s;
    }

    @Override
    public Type type() {
        return t;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("{");
        Iterator ite = terms.iterator();
        if (ite.hasNext()) {
            b.append(ite.next().toString());
        }
        while (ite.hasNext()) {
            b.append(", ").append(ite.next());
        }
        b.append('}');
        return b.toString();
    }
}
