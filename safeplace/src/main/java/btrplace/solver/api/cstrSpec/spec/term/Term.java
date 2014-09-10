package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public abstract class Term<T> {

    public abstract T eval(SpecModel mo);

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
