package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.type.Type;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class UserVar<T> extends Var<T> {

    private Term<Set> backend;

    private boolean incl;

    private T val;

    private boolean not;

    public UserVar(String lbl, boolean incl, boolean not, Term<Set> backend) {
        super(lbl);
        this.incl = incl;
        this.backend = backend;
        this.not = not;
    }

    @Override
    public Type type() {
        return incl ? backend.type().inside() : backend.type();
    }

    public boolean notBackend() {
        return not;
    }

    @Override
    public String pretty() {
        return new StringBuilder(label()).append(not ? " /" : " ").append(incl ? ": " : "<: ").append(backend).toString();
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
