package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.term.Term;

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
    public Boolean eval(Model m) {
        Object cA = a.eval(m);
        Collection cB = (Collection) b.eval(m);
        if (cB == null) {
            return null;
        }
        //System.err.println(cA + " /<:" + cB + ": " + !cB.contains(cA));
        return !cB.contains(cA);
    }
}
