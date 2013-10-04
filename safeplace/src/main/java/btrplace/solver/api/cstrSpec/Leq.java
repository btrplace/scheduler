package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.type.Nat;
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

    @Override
    public Or expand() {
        //Expect nat type with a constant
        Or or = new Or();
        if (!a.type().equals(Nat.getInstance()) || !b.type().equals(Nat.getInstance())) {
            throw new RuntimeException("Expect " + Nat.getInstance() + " <= " + Nat.getInstance() +
                    ". Got " + a.type() + " <= " + b.type());
        }
        /*
            {1,2,3} <= {1,2,4,5}
            1 <= 1
            1 <= 2
            1 <= 4
            1 <= 5
            2 <= 2
            2 <= 4
            2 <= 5
            3 <= 4
            3 <= 5
        */
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
    }

}
