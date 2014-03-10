package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

/**
 * @author Fabien Hermenier
 */
public class Eq extends AtomicProp {

    public Eq(Term a, Term b) {
        super(a, b, "=");
    }

    @Override
    public AtomicProp not() {
        return new NEq(a, b);
    }

    @Override
    public Boolean eval(SpecModel m) {
        Object vA = a.eval(m);
        Object vB = b.eval(m);
        if (vA == null && vB == null) {
            return true;
        }
        return (vA != null && vA.equals(vB));
    }
}
