package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;

import java.util.Collection;

/**
 * @author Fabien Hermenier
 */
public class Inc extends AtomicProp {

    public Inc(Term a, Term b) {
        super(a, b);
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" <: ").append(b).toString();
    }

    @Override
    public AtomicProp not() {
        return new NInc(a, b);
    }

    //@Override
    public Or expand() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean evaluate(Model m) {
        Object o = a.getValue(m);
        Collection c = (Collection) b.getValue(m);
        //System.err.println(o + " <:" + c + ": " + c.contains(o));
        if (o != null && c != null) {
            return c.contains(o);
        }
        return null;
    }

}
