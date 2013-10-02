package btrplace.solver.api.cstrSpec.func;

import btrplace.solver.api.cstrSpec.Term;
import btrplace.solver.api.cstrSpec.Value;
import btrplace.solver.api.cstrSpec.type.Type;
import btrplace.solver.api.cstrSpec.type.VMStateType;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class VMState implements Function {

    private Term t;

    public VMState(Term t) {
        this.t = t;
    }

    @Override
    public void eval() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set domain() {
        Set<Value> res = new HashSet<>();
        for (Value t : VMStateType.getInstance().getPossibleValues()) {
            Value v = new Value(t.getValue(), VMStateType.getInstance());
            res.add(v);
        }
        return res;
    }

    public String toString() {
        return "vmState(" + t + ")";
    }

    @Override
    public VMStateType type() {
        return VMStateType.getInstance();
    }
}
