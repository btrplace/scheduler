package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.specChecker.SpecModel;

/**
 * @author Fabien Hermenier
 */
public class Mult extends Term {

    private Term a, b;

    public Mult(Term t1, Term t2) {
        this.a = t1;
        this.b = t2;
    }

    @Override
    public Object eval(SpecModel mo) {
        Object o1 = a.eval(mo);
        Object o2 = b.eval(mo);
        if (o1 == null || o2 == null) {
            return null;
        }
        if (o1 instanceof Integer) {
            return (Integer) o1 * (Integer) o2;
        }
        throw new RuntimeException("Unsupported operation on '" + o1.getClass().getSimpleName() + "'");
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" * ").append(b.toString()).toString();
    }

    @Override
    public Type type() {
        return a.type();
    }
}
