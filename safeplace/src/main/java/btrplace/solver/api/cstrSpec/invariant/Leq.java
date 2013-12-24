package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;

/**
 * @author Fabien Hermenier
 */
public class Leq extends AtomicProp {

    public Leq(Term a, Term b) {
        super(a, b);
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" <= ").append(b).toString();
    }

    @Override
    public AtomicProp not() {
        return new Leq(b, a);
    }

   /* @Override
    public Or expand() {
        //Expect nat type with a constant
        Or or = new Or();
        if (!a.type().equals(NatType.getInstance()) || !b.type().equals(NatType.getInstance())) {
            throw new RuntimeException("Expect " + NatType.getInstance() + " <= " + NatType.getInstance() +
                    ". Got " + a.type() + " <= " + b.type());
        }
        for (Value vA : a.domain()) {
            for (Value vB : b.domain()) {
                int x = (Integer)vA.value();
                int y = (Integer)vB.value();
                if (x <= y) {
                    or.add(new Eq(a, vB));
                }
            }
        }
        return or;
    } */

    @Override
    public Boolean evaluate(Model m) {
        Integer iA = (Integer) a.eval(m);
        Integer iB = (Integer) b.eval(m);
        if (iA == null || iB == null) {
            return null;
        }
        return iA <= iB;
    }

}
