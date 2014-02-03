package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.spec.type.VMStateType;
import btrplace.solver.api.cstrSpec.spec.type.VMType;
import btrplace.solver.api.cstrSpec.verification.specChecker.SpecModel;

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
    public VMStateType.Type eval(SpecModel mo, List<Object> args) {
        VM v = (VM) args.get(0);
        if (v == null) {
            throw new UnsupportedOperationException();
        }
        return mo.state(v);
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
