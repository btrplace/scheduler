package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Sleeping;
import btrplace.plan.event.SuspendVM;

import java.util.UUID;

/**
 * @author Fabien Hermenier
 */
public class SleepingChecker extends DenyMyVMsActions {

    public SleepingChecker(Sleeping s) {
        super(s);
    }

    @Override
    public boolean start(SuspendVM a) {
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (UUID vm : vms) {
            if (!c.getSleepingVMs().contains(vm)) {
                return false;
            }
        }
        return true;
    }
}
