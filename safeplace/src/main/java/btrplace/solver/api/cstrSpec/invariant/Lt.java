package btrplace.solver.api.cstrSpec.invariant;

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
        if (!a.type().equals(IntType.getInstance()) || !b.type().equals(IntType.getInstance())) {
            throw new RuntimeException("Expect " + IntType.getInstance() + " < " + IntType.getInstance() +
                    ". Got " + a.type() + " <=" + b.type());
        }
        for (Constant vA : a.domain()) {
            for (Constant vB : b.domain()) {
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
        Integer vA = (Integer) a.eval(m);
        Integer vB = (Integer) b.eval(m);
        if (vA == null || vB == null) {
            return null;
        }
        return vA < vB;
    }
}
