package btrplace.solver.api.cstrSpec.spec.term;

import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.spec.type.VMType;
import btrplace.solver.api.cstrSpec.verification.specChecker.SpecModel;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class AllVMs extends Primitive {

    public AllVMs() {
        super("vms", VMType.getInstance());
    }

    @Override
    public Set<VM> eval(SpecModel m) {
        return m.VMs();
    }
}
