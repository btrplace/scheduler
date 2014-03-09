package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SetPlus extends Plus<Set<?>> {

    public SetPlus(Term<Set<?>> t1, Term<Set<?>> t2) {
        super(t1, t2);
        if (!a.type().equals(b.type())) {
            throw new RuntimeException();
        }
    }

    @Override
    public Set eval(SpecModel mo) {
        Set o1 = a.eval(mo);
        Set o2 = b.eval(mo);
        Set<?> l = new HashSet(o1);
        l.addAll(o2);
        return l;
    }
}
