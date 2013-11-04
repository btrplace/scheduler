package btrplace.solver.api.cstrSpec.func;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.Term;
import btrplace.solver.api.cstrSpec.type.VMStateType;

import java.util.Deque;

/**
 * @author Fabien Hermenier
 */
public class VMState implements Function {

    private Term t;

    public VMState(Deque<Term> stack) {
        this.t = stack.pop();
    }

    //@Override
    /*public Set<Value> domain() {
        Set<Value> res = new HashSet<>();
        for (Value t : VMStateType.getInstance().domain()) {
            Value v = new Value(t.value(), VMStateType.getInstance());
            res.add(v);
        }
        return res;
    } */

    public String toString() {
        return "vmState(" + t + ")";
    }

//    @Override
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
