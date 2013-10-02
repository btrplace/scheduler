package btrplace.solver.api.cstrSpec;

/**
 * @author Fabien Hermenier
 */
public class In extends AtomicProp {

    public In(Term a, Term b) {
        super(a, b);
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" : ").append(b).toString();
    }

    @Override
    public AtomicProp not() {
        return new NIn(a, b);
    }

    @Override
    public Or expand() {
        throw new UnsupportedOperationException();
    }
}
