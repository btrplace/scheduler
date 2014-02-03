package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.verification.specChecker.SpecModel;

/**
 * @author Fabien Hermenier
 */
public class ProtectedProposition implements Proposition {

    private Proposition p;

    public ProtectedProposition(Proposition p) {
        this.p = p;
    }

    @Override
    public Boolean eval(SpecModel m) {
        return p.eval(m);
    }

    @Override
    public Proposition not() {
        return p.not();
    }

    @Override
    public String toString() {
        return new StringBuilder("(").append(p.toString()).append(')').toString();
    }
}
