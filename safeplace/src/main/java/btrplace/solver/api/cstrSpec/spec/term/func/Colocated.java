package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.spec.type.VMType;

import java.util.Collections;
import java.util.HashSet;
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
    public Set<VM> eval(Model mo, List<Object> args) {
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
