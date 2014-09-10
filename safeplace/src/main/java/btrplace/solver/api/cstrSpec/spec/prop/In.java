package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.Collection;

/**
 * @author Fabien Hermenier
 */
public class In extends AtomicProp {

    public In(Term a, Term b) {
        super(a, b, ":");
    }

    @Override
    public AtomicProp not() {
        return new NIn(a, b);
    }

    @Override
    public Boolean eval(SpecModel m) {
        //System.err.println(this);
        //System.err.println(a.type() + " : " + b.type());
        Object o = a.eval(m);
        Collection c = (Collection) b.eval(m);
        if (c != null) {
            return c.contains(o);
        }
        return null;
    }
}
