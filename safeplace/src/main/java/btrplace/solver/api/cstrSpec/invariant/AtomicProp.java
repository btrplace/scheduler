package btrplace.solver.api.cstrSpec.invariant;

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
}
