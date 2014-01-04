package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.invariant.type.NodeType;
import btrplace.solver.api.cstrSpec.invariant.type.SetType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;
import btrplace.solver.api.cstrSpec.invariant.type.VMType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Hosted extends Function2<Set<VM>> {

    @Override
    public SetType type() {
        return new SetType(VMType.getInstance());
    }

    @Override
    public Set<VM> eval(Model mo, List<Object> args) {
        Node n = (Node) args.get(0);
        if (n == null) {
            throw new UnsupportedOperationException();
        }
        HashSet<VM> s = new HashSet<>(mo.getMapping().getRunningVMs(n));
        s.addAll(mo.getMapping().getSleepingVMs(n));
        return s;
    }

    @Override
    public String id() {
        return "hosted";
    }

    @Override
    public Type[] signature() {
        return new Type[]{NodeType.getInstance()};
    }
}
