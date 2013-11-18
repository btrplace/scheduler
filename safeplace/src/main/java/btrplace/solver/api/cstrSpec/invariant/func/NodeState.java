package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.NodeStateType;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class NodeState extends Function {

    private Term t;

    public NodeState(List<Term> stack) {
        this.t = stack.get(0);
    }

    @Override
    public String toString() {
        return "nodeState(" + t.toString() + ")";
    }

    @Override
    public NodeStateType type() {
        return NodeStateType.getInstance();
    }

    @Override
    public Object getValue(Model mo) {
        Node n = (Node) t.getValue(mo);
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
}
