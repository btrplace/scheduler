package btrplace.solver.api.cstrSpec.func;

import btrplace.solver.api.cstrSpec.type.VM;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Hoster implements Function {

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
