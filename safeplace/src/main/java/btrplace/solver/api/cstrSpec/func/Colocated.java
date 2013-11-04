package btrplace.solver.api.cstrSpec.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.Term;
import btrplace.solver.api.cstrSpec.type.VMType;

import java.util.Deque;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Colocated implements Function {

    private Term t;

    public Colocated(Deque<Term> stack) {
        this.t = stack.pop();
    }

    //@Override
    public Set domain() {
        return VMType.getInstance().domain();
    }

    //@Override
    public VMType type() {
        return VMType.getInstance();
    }

    @Override
    public Object getValue(Model mo) {
        VM v = (VM) t.getValue(mo);
        if (v == null) {
            return null;
        }
        Node n = mo.getMapping().getVMLocation(v);
        if (n == null) {
            return null;
        }
        return mo.getMapping().getRunningVMs(n); //TODO: what about sleeping VMs
    }
}
