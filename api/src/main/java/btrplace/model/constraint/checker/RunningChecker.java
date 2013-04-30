package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Running;
import btrplace.plan.RunningVMPlacement;

import java.util.UUID;

/**
 * @author Fabien Hermenier
 */
public class RunningChecker extends DenyMyVMsActions {

    public RunningChecker(Running r) {
        super(r);
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (UUID vm : vms) {
            if (!c.getRunningVMs().contains(vm)) {
                return false;
            }
        }
        return true;
    }
}
