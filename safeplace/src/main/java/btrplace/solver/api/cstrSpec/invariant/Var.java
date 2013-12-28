package btrplace.solver.api.cstrSpec.invariant;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public abstract class Var<T> implements Term<T> {

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

    public UserVariable newInclusive(String n) {
        if (type() instanceof Primitive) {
            return null;
        }
        return new UserVariable(n, true, this);
    }

    public UserVariable<Set> newPart(String n) {
        return new UserVariable(n, false, this);
    }

    @Override
    public String toString() {
        return label();
    }
}
