package btrplace.solver.api.cstrSpec.func;

import btrplace.solver.api.cstrSpec.Term;
import btrplace.solver.api.cstrSpec.type.VM;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Colocated implements Function {

    private Term t;

    public Colocated(Term t) {
        this.t = t;
    }

    @Override
    public void eval() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set domain() {
        return VM.getInstance().domain();
    }

    @Override
    public VM type() {
        return VM.getInstance();
    }
}
