package btrplace.solver.api.cstrSpec.func;

import btrplace.solver.api.cstrSpec.Term;
import btrplace.solver.api.cstrSpec.Value;
import btrplace.solver.api.cstrSpec.type.NodeType;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Host implements Function {

    private Term t;

    public Host(Term t) {
        this.t = t;
    }
    @Override
    public void eval() {
        System.err.println("Eval " + this);
    }


    @Override
    public String toString() {
        return "host(" + t + ")";
    }

    @Override
    public Set<Value> domain() {
        return NodeType.getInstance().domain();
    }

    @Override
    public NodeType type() {
        return NodeType.getInstance();
    }
}
