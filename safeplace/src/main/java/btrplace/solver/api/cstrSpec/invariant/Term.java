package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public abstract class Term<T> {

    public abstract T eval(Model mo);

    public abstract Type type();

    public UserVariable newInclusive(String n, boolean not) {
        if (type() instanceof Primitive) {
            return null;
        }
        return new UserVariable(n, true, not, this);
    }

    public UserVariable<Set> newPart(String n, boolean not) {
        return new UserVariable(n, false, not, this);
    }

}
