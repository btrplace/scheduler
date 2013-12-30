package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class UserVariable<T> extends Var<T> {

    private Term<Set> backend;

    private boolean incl;

    private T val;

    public UserVariable(String lbl, boolean incl, Term<Set> backend) {
        super(lbl);
        this.incl = incl;
        this.backend = backend;
    }

    @Override
    public Type type() {
        return incl ? backend.type().inside() : backend.type();
    }

    @Override
    public String pretty() {
        return new StringBuilder(label()).append(incl ? " : " : " <: ").append(backend).toString();
    }

    public Term getBackend() {
        return backend;
    }

    public boolean set(T o) {
        Set s = backend.eval(null);
        if (s == null || !backend.eval(null).contains(o)) {
            return false;
        }
        val = o;
        return true;
    }

    public void unset() {
        val = null;
    }

    @Override
    public T eval(Model m) {
        return val;
    }
}
