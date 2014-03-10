package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.Collection;

/**
 * @author Fabien Hermenier
 */
public class NIn extends AtomicProp {

    public NIn(Term a, Term b) {
        super(a, b, "/:");
    }

    @Override
    public AtomicProp not() {
        return new In(a, b);
    }

    @Override
    public Boolean eval(SpecModel m) {
        Object o = a.eval(m);
        Collection c = (Collection) b.eval(m);
        if (c == null) {
            return null;
        }
        return !c.contains(o);
    }
}
