package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Fence;
import btrplace.plan.RunningVMPlacement;

import java.util.Set;
import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Fence} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Fence
 */
public class FenceChecker extends AllowAllConstraintChecker {

    /**
     * Make a new checker.
     *
     * @param f the associated constraint
     */
    public FenceChecker(Fence f) {
        super(f);
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement r) {
        if (vms.contains(r.getVM())) {
            return nodes.contains(r.getDestinationNode());
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        Set<UUID> runnings = c.getRunningVMs();
        for (UUID vm : vms) {
            if (runnings.contains(vm) && !nodes.contains(c.getVMLocation(vm))) {
                return false;
            }
        }
        return true;
    }
}
