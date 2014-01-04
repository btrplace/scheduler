package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.invariant.type.Type;
import btrplace.solver.api.cstrSpec.invariant.type.VMStateType;
import btrplace.solver.api.cstrSpec.invariant.type.VMType;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class VMState extends Function<VMStateType.Type> {

    @Override
    public VMStateType type() {
        return VMStateType.getInstance();
    }

    @Override
    public VMStateType.Type eval(Model mo, List<Object> args) {
        VM v = (VM) args.get(0);
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

    @Override
    public String id() {
        return "vmState";
    }

    @Override
    public Type[] signature() {
        return new Type[]{VMType.getInstance()};
    }
}
