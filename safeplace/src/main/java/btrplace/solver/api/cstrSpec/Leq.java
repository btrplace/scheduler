package btrplace.solver.api.cstrSpec;

/**
 * @author Fabien Hermenier
 */
public class Leq extends AtomicProp {

    public Leq(Term a, Term b) {
        super(a, b);
    }

    @Override
    public String toString() {
        return new StringBuilder(a.toString()).append(" <= ").append(b).toString();
    }

    @Override
    public AtomicProp not() {
        return new Leq(b, a);
    }

    @Override
    public Or expand() {
        throw new UnsupportedOperationException();
    }

}
