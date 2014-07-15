package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

/**
 * @author Fabien Hermenier
 */
public class NEq extends AtomicProp {

    public NEq(Term a, Term b) {
        super(a, b, "/=");
    }

    @Override
    public AtomicProp not() {
        return new Eq(a, b);
    }

    @Override
    public Boolean eval(SpecModel mo) {
        Object vA = a.eval(mo);
        Object vB = b.eval(mo);
        if ((vA == null && vB != null) || (vA != null && vB == null)) {
            return true;
        }
        return vA != null && !vA.equals(vB);
    }
}
