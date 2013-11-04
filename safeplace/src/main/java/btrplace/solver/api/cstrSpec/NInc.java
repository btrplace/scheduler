package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;

import java.util.Collection;

/**
 * @author Fabien Hermenier
 */
public class NInc extends AtomicProp {

    public NInc(Term a, Term b) {
        super(a, b);
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" /<: ").append(b).toString();
    }

    @Override
    public AtomicProp not() {
        return new Inc(a, b);
    }

    /*@Override
    public Or expand() {
        throw new UnsupportedOperationException();
    } */

    @Override
    public Boolean evaluate(Model m) {
        Collection cA = (Collection) a.getValue(m);
        Collection cB = (Collection) b.getValue(m);
        if (cA == null || cB == null) {
            return null;
        }

        return !cB.containsAll(cA);
    }
}
