package btrplace.solver.api.cstrSpec;

/**
 * @author Fabien Hermenier
 */
public class NIn extends AtomicProp {

    public NIn(Term a, Term b) {
        super(a, b);
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" /: ").append(b).toString();
    }

    @Override
    public AtomicProp not() {
        return new In(a, b);
    }

    @Override
    public Or expand() {
        throw new UnsupportedOperationException();
    }

}
