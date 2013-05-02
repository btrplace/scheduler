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
public class FenceChecker extends AllowAllConstraintChecker<Fence> {

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
        if (getVMs().contains(r.getVM())) {
            return getNodes().contains(r.getDestinationNode());
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        Set<UUID> runnings = c.getRunningVMs();
        for (UUID vm : getVMs()) {
            if (runnings.contains(vm) && !getNodes().contains(c.getVMLocation(vm))) {
                return false;
            }
        }
        return true;
    }
}
