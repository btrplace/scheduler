package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

/**
 * Logical and between several propositions.
 *
 * @author Fabien Hermenier
 */
public class And extends BinaryProp {

    public And(Proposition p1, Proposition p2) {
        super(p1, p2);
    }

    @Override
    public String operator() {
        return "&";
    }

    @Override
    public Or not() {
        return new Or(p1.not(), p2.not());
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
        return r1 && r2;
    }
}