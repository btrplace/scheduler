package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.Type;
import btrplace.solver.api.cstrSpec.invariant.type.VMType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Colocated extends Function {

    private Term<VM> t;

    public static final String ID = "colocated";

    public Colocated(Term<VM> stack) {
        this.t = stack;
    }


    @Override
    public VMType type() {
        return VMType.getInstance();
    }

    @Override
    public Object eval(Model mo) {
        VM v = t.eval(mo);
        if (v == null) {
            return null;
        }
        if (mo.getMapping().isReady(v)) {
            return Collections.emptyList();
        }
        Node n = mo.getMapping().getVMLocation(v);
        if (n == null) {
            return null;
        }
        Set<VM> s = new HashSet<>(mo.getMapping().getRunningVMs(n));
        s.addAll(mo.getMapping().getSleepingVMs(n));
        return s;
    }

    @Override
    public String toString() {
        return new StringBuilder(ID).append(")").append(t).append(')').toString();
    }

    public static class Builder extends FunctionBuilder {
        @Override
        public Colocated build(List<Term> args) {
            return new Colocated(asVM(args.get(0)));
        }

        @Override
        public String id() {
            return Colocated.ID;
        }

        @Override
        public Type[] signature() {
            return new Type[]{VMType.getInstance()};
        }
    }
}
