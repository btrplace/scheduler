package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.type.ColType;
import btrplace.solver.api.cstrSpec.spec.type.IntType;
import btrplace.solver.api.cstrSpec.spec.type.Type;

import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Card extends Function<Integer> {

    @Override
    public Type type() {
        return IntType.getInstance();
    }


    @Override
    public Integer eval(Model mo, List<Object> args) {
        Collection c = (Collection) args.get(0);
        if (c == null) {
            return null;
        }
        return c.size();
    }

    @Override
    public String id() {
        return "card";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new ColType(null)};
    }
}
