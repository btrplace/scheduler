package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public abstract class Function<T> {

    public abstract String id();

    public abstract Type type();

    public Type type(List<Term> args) {
        return type();
    }

    public abstract Type[] signature();

    public Type[] signature(List<Term> args) {
        return signature();
    }

    public abstract T eval(SpecModel mo, List<Object> args);

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(id()).append('(');
        Type[] expected = signature();
        for (int i = 0; i < expected.length; i++) {
            b.append(expected[i]);
            if (i < expected.length - 1) {
                b.append(", ");
            }
        }
        return b.append(')').toString();
    }
}
