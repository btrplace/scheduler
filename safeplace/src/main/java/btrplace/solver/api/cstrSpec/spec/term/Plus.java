package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.Type;

/**
 * @author Fabien Hermenier
 */
public abstract class Plus<T> extends Term<T> {

    protected Term<T> a, b;

    public Plus(Term<T> t1, Term<T> t2) {
        this.a = t1;
        this.b = t2;
    }

    @Override
    public String toString() {
        return a.toString() + " + " + b.toString();
    }

    @Override
    public Type type() {
        return a.type();
    }
}
