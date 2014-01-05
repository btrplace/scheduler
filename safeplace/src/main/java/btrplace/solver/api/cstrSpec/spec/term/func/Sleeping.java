package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.spec.type.NodeType;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.spec.type.VMType;

import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Sleeping extends Function<Set<VM>> {

    @Override
    public SetType type() {
        return new SetType(VMType.getInstance());
    }

    @Override
    public Set<VM> eval(Model mo, List<Object> args) {
        Node n = (Node) args.get(0);
        if (n == null) {
            throw new UnsupportedOperationException();
        }
        return mo.getMapping().getSleepingVMs(n);
    }

    @Override
    public String id() {
        return "sleeping";
    }

    @Override
    public Type[] signature() {
        return new Type[]{NodeType.getInstance()};
    }
}
