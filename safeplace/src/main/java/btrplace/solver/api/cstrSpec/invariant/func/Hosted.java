package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.NodeType;
import btrplace.solver.api.cstrSpec.invariant.type.SetType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;
import btrplace.solver.api.cstrSpec.invariant.type.VMType;

import java.util.HashSet;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Hosted extends Function {

    private Term<Node> t;

    public static final String ID = "hosted";

    public Hosted(Term<Node> stack) {
        this.t = stack;
    }

    @Override
    public SetType type() {
        return new SetType(VMType.getInstance());
    }

    @Override
    public String toString() {
        return (currentValue() ? "$" : "") + new StringBuilder(ID).append("(").append(t).append(")").toString();
    }

    @Override
    public Object eval(Model mo) {
        Node n = t.eval(mo);
        if (n == null) {
            throw new UnsupportedOperationException();
        }
        HashSet<VM> s = new HashSet(mo.getMapping().getRunningVMs(n));
        s.addAll(mo.getMapping().getSleepingVMs(n));
        return s;
    }

    public static class Builder extends FunctionBuilder {
        @Override
        public Hosted build(List<Term> args) {
            return new Hosted(asNode(args.get(0)));
        }

        @Override
        public String id() {
            return Hosted.ID;
        }

        @Override
        public Type[] signature() {
            return new Type[]{NodeType.getInstance()};
        }
    }
}
