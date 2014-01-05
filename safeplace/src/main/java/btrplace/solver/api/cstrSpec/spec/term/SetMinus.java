package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.model.Model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SetMinus extends Minus<Set> {

    public SetMinus(Term<Set> t1, Term<Set> t2) {
        super(t1, t2);
    }

    @Override
    public Set eval(Model mo) {
        Set o1 = a.eval(mo);
        Set o2 = b.eval(mo);
        Set l = new HashSet();
        for (Object o : o1) {
            if (!o2.contains(o)) {
                l.add(o);
            }
        }
        return l;
    }
}
