package btrplace.solver.api.cstrSpec.invariant;

/**
 * A sequence of propositions having a same operator.
 *
 * @author Fabien Hermenier
 */
public abstract class BinaryProp implements Proposition {

    protected Proposition p1, p2;

    public BinaryProp(Proposition p1, Proposition p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        return new StringBuilder(p1.toString()).append(operator()).append(p2.toString()).toString();
    }

    public abstract String operator();

    public Proposition first() {
        return p1;
    }

    public Proposition second() {
        return p2;
    }
}
