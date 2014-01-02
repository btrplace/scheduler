package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.NodeType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;
import btrplace.solver.api.cstrSpec.invariant.type.VMType;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Host extends Function {

    private Term<VM> t;

    public static final String ID = "host";

    public Host(Term<VM> t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return (currentValue() ? "$" : "") + ID + "(" + t + ")";
    }

    @Override
    public NodeType type() {
        return NodeType.getInstance();
    }

    /*public Or eq(Term t) {
        Or o = new Or();
        Set<Constant> dom = domain();
        for (Constant v : t.domain()) {
            if (dom.contains(v)) {
                o.add(new Eq(this, t));
            }
        }
        return o;
    }       */

    @Override
    public Object eval(Model mo) {
        VM vm = t.eval(mo); //It's a vmId;
        if (vm == null) {
            throw new UnsupportedOperationException();
        }
        return mo.getMapping().getVMLocation(vm);
    }

    public static class Builder extends FunctionBuilder {
        @Override
        public Host build(List<Term> args) {
            return new Host(asVM(args.get(0)));
        }

        @Override
        public String id() {
            return Host.ID;
        }

        @Override
        public Type[] signature() {
            return new Type[]{VMType.getInstance()};
        }
    }
}
