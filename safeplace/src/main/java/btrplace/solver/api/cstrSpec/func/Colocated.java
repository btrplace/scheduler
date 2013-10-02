package btrplace.solver.api.cstrSpec.func;

import btrplace.solver.api.cstrSpec.Term;
import btrplace.solver.api.cstrSpec.type.Type;
import btrplace.solver.api.cstrSpec.type.VM;
import btrplace.solver.api.cstrSpec.type.VMStateType;

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
        return VM.getInstance().getPossibleValues();
    }

    @Override
    public VM type() {
        return VM.getInstance();
    }
}
