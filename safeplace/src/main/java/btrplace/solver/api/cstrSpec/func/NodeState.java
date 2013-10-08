package btrplace.solver.api.cstrSpec.func;

import btrplace.solver.api.cstrSpec.Term;
import btrplace.solver.api.cstrSpec.Value;
import btrplace.solver.api.cstrSpec.type.NodeStateType;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NodeState implements Function {

    private Term t;

    public NodeState(Term t) {
        this.t = t;
    }

    @Override
    public void eval() {
        System.err.println("Eval " + toString());
    }

    public String toString() {
        return "nodeState(" + t.toString() + ")";
    }

    @Override
    public Set<Value> domain() {
        return NodeStateType.getInstance().domain();
    }

    @Override
    public NodeStateType type() {
        return NodeStateType.getInstance();
    }

}
