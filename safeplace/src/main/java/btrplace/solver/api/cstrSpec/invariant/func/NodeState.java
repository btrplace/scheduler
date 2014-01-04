package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.solver.api.cstrSpec.invariant.type.NodeStateType;
import btrplace.solver.api.cstrSpec.invariant.type.NodeType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class NodeState extends Function2<NodeStateType.Type> {

    @Override
    public NodeStateType type() {
        return NodeStateType.getInstance();
    }

    @Override
    public NodeStateType.Type eval(Model mo, List<Object> args) {
        Node n = (Node) args.get(0);
        if (n == null) {
            return null;
        }
        if (mo.getMapping().isOffline(n)) {
            return NodeStateType.Type.offline;
        } else if (mo.getMapping().isOnline(n)) {
            return NodeStateType.Type.online;
        } else {
            return null;
        }
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
