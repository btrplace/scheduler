package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.invariant.type.NodeType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;
import btrplace.solver.api.cstrSpec.invariant.type.VMType;

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
    public Node eval(Model mo, List<Object> args) {
        VM vm = (VM) args.get(0);
        if (vm == null) {
            throw new UnsupportedOperationException();
        }
        return mo.getMapping().getVMLocation(vm);
    }

    public Type[] signature() {
        return new Type[]{VMType.getInstance()};
    }
}
