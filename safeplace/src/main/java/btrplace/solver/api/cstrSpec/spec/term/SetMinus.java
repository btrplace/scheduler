package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SetMinus extends Minus<Set> {

    public SetMinus(Term<Set> t1, Term<Set> t2) {
        super(t1, t2);
        if (!a.type().equals(b.type())) {
            throw new RuntimeException();
        }
    }

    @Override
    public Set eval(SpecModel mo) {
        Collection o1 = a.eval(mo);
        Collection o2 = b.eval(mo);
        Set l = new HashSet();
        for (Object o : o1) {
            if (!o2.contains(o)) {
                l.add(o);
            }
        }
        return l;
    }
}
