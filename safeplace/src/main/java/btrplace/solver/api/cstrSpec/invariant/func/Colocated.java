package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.VMType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Colocated extends Function {

    private Term t;

    public Colocated(List<Term> stack) {
        this.t = stack.get(0);
    }


    @Override
    public VMType type() {
        return VMType.getInstance();
    }

    @Override
    public Object getValue(Model mo) {
        VM v = (VM) t.getValue(mo);
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
        return new StringBuilder("colocated(").append(t).append(')').toString();
    }
}
