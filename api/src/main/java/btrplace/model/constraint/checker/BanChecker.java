package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Ban;
import btrplace.plan.RunningVMPlacement;

import java.util.Set;
import java.util.UUID;

/**
 * Checker for the {@link Ban} constraint
 *
 * @author Fabien Hermenier
 * @see Ban
 */
public class BanChecker extends AllowAllConstraintChecker {

    /**
     * Make a new checker.
     *
     * @param b the associated constraint
     */
    public BanChecker(Ban b) {
        super(b);
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement r) {
        if (vms.contains(r.getVM())) {
            return !nodes.contains(r.getDestinationNode());
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        Set<UUID> runnings = c.getRunningVMs();
        for (UUID vm : vms) {
            if (runnings.contains(vm) && nodes.contains(c.getVMLocation(vm))) {
                return false;
            }
        }
        return true;
    }
}
