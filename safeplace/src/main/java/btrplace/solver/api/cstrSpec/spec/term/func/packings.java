package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.util.AllPackingsGenerator;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Packings extends Function<Set> {

    @Override
    public Type type() {
        return new SetType(new SetType(null));
    }

    public Set eval(SpecModel mo, List<Object> args) {
        return allPacking((Collection) args.get(0));
    }


    private Set<Set<Set<Object>>> allPacking(Collection<Object> args) {
        AllPackingsGenerator<Object> pg = new AllPackingsGenerator<>(Object.class, args);
        Set<Set<Set<Object>>> packings = new HashSet<>();
        while (pg.hasNext()) {
            Set<Set<Object>> s = pg.next();
            if (!s.isEmpty()) {
                packings.add(s);
            }
        }
        return packings;
    }

    @Override
    public String id() {
        return "packings";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new SetType(null)};
    }

    @Override
    public Type type(List<Term> args) {
        return new SetType(new SetType(args.get(0).type()));
    }
}
