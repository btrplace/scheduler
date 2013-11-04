package btrplace.solver.api.cstrSpec.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.Term;
import btrplace.solver.api.cstrSpec.type.NodeType;

import java.util.Deque;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Host implements Function {

    private Term t;

    public Host(Deque<Term> stack) {
        this.t = stack.pop();
    }

    @Override
    public String toString() {
        return "host(" + t + ")";
    }

    //@Override
    public Set<Node> domain() {
        return NodeType.getInstance().domain();
    }

    //@Override
    public NodeType type() {
        return NodeType.getInstance();
    }

    /*public Or eq(Term t) {
        Or o = new Or();
        Set<Value> dom = domain();
        for (Value v : t.domain()) {
            if (dom.contains(v)) {
                o.add(new Eq(this, t));
            }
        }
        return o;
    }       */

    @Override
    public Object getValue(Model mo) {
        VM vm = (VM) t.getValue(mo); //It's a vmId;
        if (vm == null) {
            throw new UnsupportedOperationException();
        }
        return mo.getMapping().getVMLocation(vm);
    }
}
