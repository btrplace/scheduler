package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.spec.type.NodeType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.spec.type.VMType;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Host extends Function<Node> {

    @Override
    public String id() {
        return "host";
    }

    @Override
    public NodeType type() {
        return NodeType.getInstance();
    }

    @Override
    public Node eval(SpecModel mo, List<Object> args) {
        VM vm = (VM) args.get(0);
        if (vm == null) {
            throw new UnsupportedOperationException();
        }
        return mo.host(vm);
    }

    public Type[] signature() {
        return new Type[]{VMType.getInstance()};
    }
}
