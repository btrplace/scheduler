package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.util.AllTuplesGenerator;

import java.util.*;

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
        List c = new ArrayList((Collection) args.get(0));
        List<List<Object>> l = new ArrayList<>();
        for (int i = 0; i < c.size(); i++) {
            l.add(c);
        }
        AllTuplesGenerator<Object> tg = new AllTuplesGenerator<>(Object.class, l);
        Set<Set> res = new HashSet<>();
        while (tg.hasNext()) {
            Object[] in = tg.next();
            Set<Object> s = new HashSet<>(in.length);
            Collections.addAll(s, in);
            res.add(s);
        }
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

    @Override
    public Type type(List<Term> args) {
        return new SetType(new SetType(args.get(0).type()));
    }
}
