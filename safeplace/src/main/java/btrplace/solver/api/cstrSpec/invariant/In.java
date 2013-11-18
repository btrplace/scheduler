package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;

import java.util.Collection;

/**
 * @author Fabien Hermenier
 */
public class In extends AtomicProp {

    public In(Term a, Term b) {
        super(a, b);
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" : ").append(b).toString();
    }

    @Override
    public AtomicProp not() {
        return new NIn(a, b);
    }

    @Override
    public Boolean evaluate(Model m) {
        Object o = a.getValue(m);
        Collection c = (Collection) b.getValue(m);
        if (c != null) {
            return c.contains(o);
        }
        return null;
    }

    /*@Override
    public Or expand() {
        throw new UnsupportedOperationException();
    } */
}
