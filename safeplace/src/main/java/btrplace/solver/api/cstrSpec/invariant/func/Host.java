package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.NodeType;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Host extends Function {

    private Term t;

    public Host(List<Term> stack) {
        this.t = stack.get(0);
    }

    @Override
    public String toString() {
        return (currentValue() ? "$" : "") + "host(" + t + ")";
    }

    @Override
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
