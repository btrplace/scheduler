package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.specChecker.SpecModel;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class ProtectedTerm<T> extends Term<T> {

    private Term<T> t;

    public ProtectedTerm(Term<T> t) {
        this.t = t;
    }

    @Override
    public T eval(SpecModel mo) {
        return t.eval(mo);
    }

    @Override
    public Type type() {
        return t.type();
    }

    @Override
    public UserVar newInclusive(String n, boolean not) {
        return t.newInclusive(n, not);
    }

    @Override
    public UserVar<Set> newPart(String n, boolean not) {
        return t.newPart(n, not);
    }

    @Override
    public String toString() {
        return new StringBuilder("(").append(t.toString()).append(')').toString();
    }
}
