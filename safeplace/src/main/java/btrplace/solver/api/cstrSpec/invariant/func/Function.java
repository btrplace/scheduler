package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public abstract class Function<T> {

    public abstract String id();

    public abstract Type type();

    public abstract Type[] signature();

    public abstract T eval(Model mo, List<Object> args);

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
