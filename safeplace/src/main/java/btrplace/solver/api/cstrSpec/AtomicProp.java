package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;

/**
 * @author Fabien Hermenier
 */
public abstract class AtomicProp implements Proposition {

    protected Term a, b;

    public AtomicProp(Term a, Term b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean inject(Model mo) {
        throw new UnsupportedOperationException();
    }
}
