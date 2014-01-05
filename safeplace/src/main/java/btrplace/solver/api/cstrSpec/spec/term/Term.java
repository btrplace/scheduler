package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.type.Type;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public abstract class Term<T> {

    public abstract T eval(Model mo);

    public abstract Type type();

    public UserVar newInclusive(String n, boolean not) {
        if (type() instanceof Primitive) {
            return null;
        }
        return new UserVar(n, true, not, this);
    }

    public UserVar<Set> newPart(String n, boolean not) {
        return new UserVar(n, false, not, this);
    }

}
