package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.Type;
import btrplace.solver.api.cstrSpec.invariant.type.VMStateType;
import btrplace.solver.api.cstrSpec.invariant.type.VMType;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class VMState extends Function {

    private Term<VM> t;

    public static final String ID = "vmState";

    public VMState(Term<VM> vm) {
        this.t = vm;
    }

    @Override
    public String toString() {
        return (currentValue() ? "$" : "") + ID + "(" + t + ")";
    }

    @Override
    public VMStateType type() {
        return VMStateType.getInstance();
    }

    @Override
    public Object eval(Model mo) {
        VM v = (VM) t.eval(mo);
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

    public static class Builder extends FunctionBuilder {
        @Override
        public VMState build(List<Term> args) {
            return new VMState(asVM(args.get(0)));
        }

        @Override
        public String id() {
            return VMState.ID;
        }

        @Override
        public Type[] signature() {
            return new Type[]{VMType.getInstance()};
        }
    }

}
