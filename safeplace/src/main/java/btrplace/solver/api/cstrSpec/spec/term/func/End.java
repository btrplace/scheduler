package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.plan.event.Action;
import btrplace.solver.api.cstrSpec.spec.type.ActionType;
import btrplace.solver.api.cstrSpec.spec.type.TimeType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.List;

/**
 * Get the moment an action ends.
 *
 * @author Fabien Hermenier
 */
public class End extends Function<Integer> {

    @Override
    public TimeType type() {
        return TimeType.getInstance();
    }

    @Override
    public Integer eval(SpecModel mo, List<Object> args) {
        Action a = (Action) args.get(0);
        if (a == null) {
            throw new UnsupportedOperationException();
        }
        return a.getEnd();
    }

    @Override
    public String id() {
        return "end";
    }

    @Override
    public Type[] signature() {
        return new Type[]{ActionType.getInstance()};
    }
}
