package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;

/**
 * @author Fabien Hermenier
 */
public class Not implements Proposition {

    private Proposition p;

    public Not(Proposition p) {
        this.p = p;
    }

    @Override
    public Proposition not() {
        return p;
    }

    @Override
    public Boolean evaluate(Model m) {
        return !p.evaluate(m);
    }

    @Override
    public String toString() {
        return "~" + p;
    }
}
