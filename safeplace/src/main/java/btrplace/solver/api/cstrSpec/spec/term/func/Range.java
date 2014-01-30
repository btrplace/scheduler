package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.type.IntType;
import btrplace.solver.api.cstrSpec.spec.type.ListType;
import btrplace.solver.api.cstrSpec.spec.type.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Get all the indexes of a given list.
 *
 * @author Fabien Hermenier
 */
public class Range extends Function<List<Integer>> {

    @Override
    public Type type() {
        return new ListType(IntType.getInstance());
    }


    @Override
    public List<Integer> eval(Model mo, List<Object> args) {
        List c = (List) args.get(0);
        if (c == null) {
            return null;
        }
        List<Integer> res = new ArrayList<>(c.size());
        for (int i = 0; i < c.size(); i++) {
            res.add(i);
        }
        return res;
    }

    @Override
    public String id() {
        return "range";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new ListType(null)};
    }
}
