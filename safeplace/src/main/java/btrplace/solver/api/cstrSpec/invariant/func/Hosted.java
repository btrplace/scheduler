package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.VMType;

import java.util.HashSet;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Hosted extends Function {

    private Term t;

    public Hosted(List<Term> stack) {
        this.t = stack.get(0);
    }

    @Override
    public VMType type() {
        return VMType.getInstance();
    }

    @Override
    public String toString() {
        return (currentValue() ? "$" : "") + new StringBuilder("hoster(").append(t).append(")").toString();
    }

    @Override
    public Object eval(Model mo) {
        Node n = (Node) t.eval(mo);
        if (n == null) {
            throw new UnsupportedOperationException();
        }
        HashSet<VM> s = new HashSet(mo.getMapping().getRunningVMs(n));
        s.addAll(mo.getMapping().getSleepingVMs(n));
        return s;
    }
}
