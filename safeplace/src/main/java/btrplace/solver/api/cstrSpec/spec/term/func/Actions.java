package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.VM;
import btrplace.plan.event.Action;
import btrplace.plan.event.VMEvent;
import btrplace.solver.api.cstrSpec.spec.type.ActionType;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.spec.type.VMType;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Get all the actions that manipulate a VM.
 *
 * @author Fabien Hermenier
 */
public class Actions extends Function<Set<Action>> {

    @Override
    public Type type() {
        return new SetType(ActionType.getInstance());
    }


    @Override
    public Set<Action> eval(SpecModel mo, List<Object> args) {
        VM v = (VM) args.get(0);
        if (v == null) {
            throw new UnsupportedOperationException();
        }
        Set<Action> s = new HashSet<>();
        for (Action a : mo.getPlan()) {
            if (a instanceof VMEvent) {
                if (((VMEvent) a).getVM().equals(v)) {
                    s.add(a);
                }
            }
        }
        return s;
    }

    @Override
    public String id() {
        return "actions";
    }

    @Override
    public Type[] signature() {
        return new Type[]{VMType.getInstance()};
    }
}
