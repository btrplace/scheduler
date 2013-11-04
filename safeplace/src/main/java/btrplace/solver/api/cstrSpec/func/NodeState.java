package btrplace.solver.api.cstrSpec.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.solver.api.cstrSpec.Term;
import btrplace.solver.api.cstrSpec.Value;
import btrplace.solver.api.cstrSpec.type.NodeStateType;

import java.util.Deque;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NodeState implements Function {

    private Term t;

    public NodeState(Deque<Term> stack) {
        this.t = stack.pop();
    }

    public String toString() {
        return "nodeState(" + t.toString() + ")";
    }

    public Set<Value> domain() {
        return NodeStateType.getInstance().domain();
    }

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
