package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;

/**
 * @author Fabien Hermenier
 */
public class Lt extends AtomicProp {

    public Lt(Term a, Term b) {
        super(a, b);
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" < ").append(b).toString();
    }

    @Override
    public AtomicProp not() {
        return new Lt(b, a);
    }

    /*@Override
    public Or expand() {
        //Expect nat type with a constant
        Or or = new Or();
        if (!a.type().equals(NatType.getInstance()) || !b.type().equals(NatType.getInstance())) {
            throw new RuntimeException("Expect " + NatType.getInstance() + " < " + NatType.getInstance() +
                    ". Got " + a.type() + " <=" + b.type());
        }
        for (Value vA : a.domain()) {
            for (Value vB : b.domain()) {
                int x = (Integer)vA.value();
                int y = (Integer)vB.value();
                if (x < y) {
                    or.add(new Eq(a, vB));
                }
            }
        }
        return or;
    }    */

    @Override
    public Boolean evaluate(Model m) {
        Integer vA = (Integer)a.getValue(m);
        Integer vB = (Integer)b.getValue(m);
        if (vA == null || vB == null) {
            return null;
        }
        return vA < vB;
    }
}
