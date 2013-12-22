package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.VMStateType;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class VMState extends Function {

    private Term t;

    public VMState(List<Term> stack) {
        this.t = stack.get(0);
    }

    @Override
    public String toString() {
        return (currentValue() ? "$" : "") + "vmState(" + t + ")";
    }

    @Override
    public VMStateType type() {
        return VMStateType.getInstance();
    }

    @Override
    public Object getValue(Model mo) {
        VM v = (VM) t.getValue(mo);
        if (v == null) {
            throw new UnsupportedOperationException();
        }
        if (mo.getMapping().isRunning(v)) {
            return VMStateType.Type.running;
        } else if (mo.getMapping().isSleeping(v)) {
            return VMStateType.Type.sleeping;
        } else if (mo.getMapping().isReady(v)) {
            return VMStateType.Type.ready;
        }
        return null;
    }
}
