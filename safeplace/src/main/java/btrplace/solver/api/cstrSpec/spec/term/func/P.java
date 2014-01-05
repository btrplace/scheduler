package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;

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

    /*@Override
    public Type[] signature(List<Term> args) {
        return new Type[]{new SetType((args.get(0).type()))};
    } */

    @Override
    public Type type(List<Term> args) {
        return new SetType(new SetType(args.get(0).type()));
    }
}
