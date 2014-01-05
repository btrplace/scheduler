package btrplace.solver.api.cstrSpec.spec.prop;

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
    public Boolean eval(Model m) {
        return !p.eval(m);
    }

    @Override
    public String toString() {
        return "~" + p;
    }
}
