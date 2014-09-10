package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

/**
 * @author Fabien Hermenier
 */
public class Lt extends AtomicProp {

    public Lt(Term a, Term b) {
        super(a, b, "<");
    }

    @Override
    public AtomicProp not() {
        return new Lt(b, a);
    }

    @Override
    public Boolean eval(SpecModel m) {
        Integer vA = (Integer) a.eval(m);
        Integer vB = (Integer) b.eval(m);
        if (vA == null || vB == null) {
            return null;
        }
        return vA < vB;
    }
}
