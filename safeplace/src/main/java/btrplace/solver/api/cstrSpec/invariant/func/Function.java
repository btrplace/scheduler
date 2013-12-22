package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.solver.api.cstrSpec.invariant.Term;

/**
 * @author Fabien Hermenier
 */
public abstract class Function implements Term {

    private boolean cur;

    public Function() {
        cur = false;
    }

    public void currentValue(boolean b) {
        this.cur = b;
    }

    public boolean currentValue() {
        return cur;
    }
}
