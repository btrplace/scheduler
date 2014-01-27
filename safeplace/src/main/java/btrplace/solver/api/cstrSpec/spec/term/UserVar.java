package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.type.Type;

import java.util.Collection;

/**
 * @author Fabien Hermenier
 */
public class UserVar<T> extends Var<T> {

    private Term<Collection> backend;

    private boolean incl;

    private T val;

    private boolean not;

    public UserVar(String lbl, boolean incl, boolean not, Term backend) {
        super(lbl);
        this.incl = incl;
        this.backend = backend;
        this.not = not;
    }

    @Override
    public Type type() {
        return incl ? backend.type().inside() : backend.type();
    }

    @Override
    public String pretty() {
        return new StringBuilder(label()).append(not ? " /" : " ").append(incl ? ": " : "<: ").append(backend).toString();
    }

    public Term<Collection> getBackend() {
        return backend;
    }

    public boolean set(Model mo, T o) {
        Collection s = backend.eval(mo);
        if (s == null) {
            throw new IllegalArgumentException("'" + o + "' is outside the domain of  " + backend + " (" + backend.eval(mo) + ")");
        }
        if (incl && !backend.eval(mo).contains(o)) {
            throw new IllegalArgumentException("'" + o + "' is outside the domain of  " + backend + " (" + backend.eval(mo) + ")");
        } else if (!incl && !backend.eval(mo).containsAll((Collection) o)) {
            throw new IllegalArgumentException("'" + o + "' is outside the domain of  " + backend + " (" + backend.eval(mo) + ")");
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
