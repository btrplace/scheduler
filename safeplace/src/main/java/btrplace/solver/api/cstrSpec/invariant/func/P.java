package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.type.SetType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class P extends Function<Set> {

    @Override
    public Type type() {
        return new SetType(new SetType(null));
    }

    @Override
    public Set eval(Model mo, List<Object> args) {
        Set c = (Set) args.get(0);
        Set<Set> res = new HashSet();
        return res;
    }

    @Override
    public String id() {
        return "P";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new SetType(null)};
    }
}
