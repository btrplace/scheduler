package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

/**
 * @author Fabien Hermenier
 */
public class Eq extends AtomicProp {

    public Eq(Term a, Term b) {
        super(a, b);
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" = ").append(b).toString();
    }

    @Override
    public AtomicProp not() {
        return new NEq(a, b);
    }

    @Override
    public Boolean eval(SpecModel m) {
        Object vA = a.eval(m);
        Object vB = b.eval(m);
        if (vA == null && vB == null) {
            return true;
        }
        return (vA != null && vA.equals(vB));
    }

    /*@Override
    public Or expand() {
        Or or = new Or();
        if (a.domain().size() > 1 && b.domain().size() > 1) {
            // {1,2} ={1,2} == {1} = {1} | {2} = {2}
            for (Constant i : a.domain()) {
                for (Constant j : b.domain()) {
                    if (i.equals(j)) {
                        or.add(new Eq(a, j));//.add(new Eq(b, i));
                    }
                }
            }
        } else {
            or.add(this);
        }
        return or;
    }       */
}
