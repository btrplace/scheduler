package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Running;
import btrplace.plan.RunningVMPlacement;

import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Running} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Running
 */
public class RunningChecker extends DenyMyVMsActions {

    /**
     * Make a new checker.
     *
     * @param r the associated constraint
     */
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
