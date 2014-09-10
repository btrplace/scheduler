package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

/**
 * @author Fabien Hermenier
 */
public class Or extends BinaryProp {

    public Or(Proposition p1, Proposition p2) {
        super(p1, p2);
    }

    @Override
    public String operator() {
        return "|";
    }

    @Override
    public And not() {
        return new And(p1.not(), p2.not());
    }

    @Override
    public Boolean eval(SpecModel m) {
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

    @Override
    public Proposition simplify(SpecModel m) {
        return new Or(p1.simplify(m), p2.simplify(m));
    }

    @Override
    public String toString() {
        if (p1 == Proposition.False) {
            return p2.toString();
        }
        if (p2 == Proposition.False) {
            return p1.toString();
        }
        return super.toString();
    }
}
