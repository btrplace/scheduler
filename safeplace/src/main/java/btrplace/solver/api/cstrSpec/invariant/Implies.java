package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;

/**
 * @author Fabien Hermenier
 */
public class Implies implements Proposition {

    private Or o;

    private Proposition p1, p2;

    public Implies(Proposition p1, Proposition p2) {
        this.p1 = p1;
        this.p2 = p2;
        o = new Or().add(p1.not()).add(p2);
    }

    @Override
    public String toString() {
        return new StringBuilder(p1.toString()).append(" --> ").append(p2.toString()).toString();
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public And not() {
        return o.not();
    }

    /*@Override
    public Or expand() {
        Or or = new Or();
        for (Proposition p : this.props) {
            or.add(p.expand());
        }
        return or;
    } */

    @Override
    public Boolean evaluate(Model m) {
        return o.evaluate(m);
    }
}
