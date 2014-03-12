package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

/**
 * @author Fabien Hermenier
 */
public class Leq extends AtomicProp {

    public Leq(Term a, Term b) {
        super(a, b, "<=");
    }

    @Override
    public AtomicProp not() {
        return new Lt(b, a);
    }

    @Override
    public Boolean eval(SpecModel m) {
        Integer iA = (Integer) a.eval(m);
        Integer iB = (Integer) b.eval(m);
        if (iA == null || iB == null) {
            return null;
        }
        return iA <= iB;
    }

}
