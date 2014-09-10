package btrplace.solver.api.cstrSpec.spec.term.func;

import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.spec.type.VMType;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import edu.emory.mathcs.backport.java.util.Collections;

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
    public Set<VM> eval(SpecModel mo, List<Object> args) {
        VM v = (VM) args.get(0);
        if (v == null) {
            return null;
        }
        Node n = mo.getMapping().host(v);
        if (n == null) {
            return Collections.emptySet();
        }
        Set<VM> vms = new HashSet<>();
        vms.addAll(mo.getMapping().sleeping(n));
        vms.addAll(mo.getMapping().runnings(n));
        return vms;
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
