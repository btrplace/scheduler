package btrplace.solver.api.cstrSpec.invariant;

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

    public abstract boolean set(T o);

    public abstract void unset();

    @Override
    public String toString() {
        return label();
    }
}
