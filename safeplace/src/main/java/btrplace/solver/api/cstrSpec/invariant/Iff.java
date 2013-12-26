package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;

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
        return " <--> ";
    }

    @Override
    public And not() {
        return o.not();
    }

    @Override
    public Boolean evaluate(Model m) {
        return o.evaluate(m);
    }
}
