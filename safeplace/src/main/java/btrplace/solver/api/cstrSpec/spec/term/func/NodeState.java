package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.Node;
import btrplace.solver.api.cstrSpec.spec.type.NodeStateType;
import btrplace.solver.api.cstrSpec.spec.type.NodeType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class NodeState extends Function<NodeStateType.Type> {

    @Override
    public NodeStateType type() {
        return NodeStateType.getInstance();
    }

    @Override
    public NodeStateType.Type eval(SpecModel mo, List<Object> args) {

        Node n = (Node) args.get(0);
        if (n == null) {
            return null;
        }
        return mo.getMapping().state(n);
        /*if (mo.getMapping().isOffline(n)) {
            return NodeStateType.Type.offline;
        } else if (mo.getMapping().isOnline(n)) {
            return NodeStateType.Type.online;
        } else {
            return null;
        } */
    }

    @Override
    public String id() {
        return "nodeState";
    }

    @Override
    public Type[] signature() {
        return new Type[]{NodeType.getInstance()};
    }
}
