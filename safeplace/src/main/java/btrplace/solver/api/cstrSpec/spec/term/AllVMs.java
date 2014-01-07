package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.spec.type.VMType;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class AllVMs extends Primitive {

    public AllVMs() {
        super("vms", VMType.getInstance());
    }

    @Override
    public Set<VM> eval(Model m) {
        return m.getMapping().getAllVMs();
    }
}
