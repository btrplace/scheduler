package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;

/**
 * @author Fabien Hermenier
 */
public class Or extends BinaryProp {

    public Or(Proposition p1, Proposition p2) {
        super(p1, p2);
    }

    @Override
    public String operator() {
        return " | ";
    }

    @Override
    public And not() {
        return new And(p1.not(), p2.not());
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
    public Boolean eval(Model m) {
        Boolean r1 = p1.eval(m);
        if (r1 == null) {
            return null;
        }
        Boolean r2 = p2.eval(m);
        if (r2 == null) {
            return null;
        }
        return r1 || r2;
    }
}
