package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.NodeStateType;
import btrplace.solver.api.cstrSpec.invariant.type.NodeType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class NodeState extends Function {

    private Term<Node> t;

    public static final String ID = "nodeState";

    public NodeState(Term<Node> stack) {
        this.t = stack;
    }

    @Override
    public String toString() {
        return (currentValue() ? "$" : "") + "nodeState(" + t.toString() + ")";
    }

    @Override
    public NodeStateType type() {
        return NodeStateType.getInstance();
    }

    @Override
    public Object eval(Model mo) {
        Node n = (Node) t.eval(mo);
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

    public static class Builder extends FunctionBuilder {
        @Override
        public NodeState build(List<Term> args) {
            return new NodeState(asNode(args.get(0)));
        }

        @Override
        public String id() {
            return NodeState.ID;
        }

        @Override
        public Type[] signature() {
            return new Type[]{NodeType.getInstance()};
        }
    }
}
