package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Killed;
import btrplace.plan.event.KillVM;

import java.util.UUID;

/**
 * @author Fabien Hermenier
 */
public class KilledChecker extends DenyMyVMsActions {

    public KilledChecker(Killed k) {
        super(k);
    }

    @Override
    public boolean start(KillVM a) {
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (UUID vm : vms) {
            if (c.getAllVMs().contains(vm)) {
                return false;
            }
        }
        return true;
    }
}
