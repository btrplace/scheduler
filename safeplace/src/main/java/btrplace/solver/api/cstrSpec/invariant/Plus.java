package btrplace.solver.api.cstrSpec.invariant;

import btrplace.solver.api.cstrSpec.invariant.type.Type;

/**
 * @author Fabien Hermenier
 */
public abstract class Plus<T> implements Term<T> {

    protected Term<T> a, b;

    public Plus(Term<T> t1, Term<T> t2) {
        if (!t1.type().equals(t2.type())) {
            throw new RuntimeException("Unconsistent addition " + t1.type() + " + " + t2.type());
        }
        this.a = t1;
        this.b = t2;
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" + ").append(b.toString()).toString();
    }

    @Override
    public Type type() {
        return a.type();
    }
}
