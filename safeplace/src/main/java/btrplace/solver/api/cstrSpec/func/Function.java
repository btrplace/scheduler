package btrplace.solver.api.cstrSpec.func;

import btrplace.solver.api.cstrSpec.Term;

/**
 * @author Fabien Hermenier
 */
public abstract class Function implements Term {

    @Override
    public Term plus(Term t2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Term minus(Term t2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Term mult(Term t2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Term div(Term t2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Term inter(Term t2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Term union(Term t2) {
        throw new UnsupportedOperationException();
    }

}
