package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.spec.type.VMType;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Colocated extends Function<Set<VM>> {

    @Override
    public SetType type() {
        return new SetType(VMType.getInstance());
    }

    @Override
    public Set<VM> eval(SpecModel mo, List<Object> args) {
        /*
        VM v = (VM) args.get(0);
        if (v == null) {
            return null;
        }
        if (mo.getMapping().isReady(v)) {
            return Collections.emptySet();
        }
        Node n = mo.getMapping().getVMLocation(v);
        if (n == null) {
            return null;
        }
        Set<VM> s = new HashSet<>(mo.getMapping().getRunningVMs(n));
        s.addAll(mo.getMapping().getSleepingVMs(n));
        return s;
        */
        throw new UnsupportedOperationException();
    }

    @Override
    public String id() {
        return "colocated";
    }

    @Override
    public Type[] signature() {
        return new Type[]{VMType.getInstance()};
    }
}
