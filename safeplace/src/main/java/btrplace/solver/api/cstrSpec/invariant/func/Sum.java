package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.type.IntType;
import btrplace.solver.api.cstrSpec.invariant.type.SetType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Sum extends Function<Integer> {

    @Override
    public Type type() {
        return IntType.getInstance();
    }

    @Override
    public Integer eval(Model mo, List<Object> args) {
        Collection<Integer> c = (Collection<Integer>) args.get(0);
        if (c == null) {
            return null;
        }
        int s = 0;
        for (Integer i : c) {
            s += i;
        }
        return s;
    }

    @Override
    public String id() {
        return "sum";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new SetType(IntType.getInstance())};
    }
}
