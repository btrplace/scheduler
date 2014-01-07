package btrplace.solver.api.cstrSpec.spec.term;

/**
 * @author Fabien Hermenier
 */
public abstract class Var<T> extends Term<T> {

    private String lbl;

    public Var(String n) {
        this.lbl = n;
    }

    public String label() {
        return lbl;
    }

    public String pretty() {
        return toString();
    }

    @Override
    public String toString() {
        return label();
    }
}
