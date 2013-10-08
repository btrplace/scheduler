package btrplace.solver.api.cstrSpec.func;

import btrplace.solver.api.cstrSpec.Term;
import btrplace.solver.api.cstrSpec.type.VMType;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Hoster implements Function {

    private Term t;

    public Hoster(Term t) {
        this.t = t;
    }

    @Override
    public void eval() {
        System.err.println("Eval " + t);
    }

    @Override
    public Set domain() {
        return VMType.getInstance().domain();
    }

    @Override
    public VMType type() {
        return VMType.getInstance();
    }

    @Override
    public String toString() {
        return new StringBuilder("hoster(").append(t).append(")").toString();
    }
}
