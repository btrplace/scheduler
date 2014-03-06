package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

/**
 * @author Fabien Hermenier
 */
public class Iff extends BinaryProp {

    private Or o;


    public Iff(Proposition p1, Proposition p2) {
        super(p1, p2);
        o = new Or(new And(p1, p2), new And(p1.not(), p2.not()));
    }

    @Override
    public String operator() {
        return "<-->";
    }

    @Override
    public And not() {
        return o.not();
    }

    @Override
    public Boolean eval(SpecModel m) {
        return o.eval(m);
    }
}
