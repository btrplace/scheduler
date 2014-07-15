package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

/**
 * @author Fabien Hermenier
 */
public abstract class AtomicProp implements Proposition {

    protected Term a, b;

    private String op;

    public AtomicProp(Term a, Term b, String op) {
        this.a = a;
        this.b = b;
        this.op = op;
    }

    @Override
    public String toString() {
        return a.toString() + " " + op + " " + b.toString();
    }

    @Override
    public Proposition simplify(SpecModel m) {
        return this;
    }
}
