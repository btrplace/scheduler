package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Gather;
import btrplace.plan.RunningVMPlacement;

import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Gather} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Gather
 */
public class GatherChecker extends AllowAllConstraintChecker {

    /**
     * Make a new checker.
     *
     * @param g the associated constraint
     */
    public GatherChecker(Gather g) {
        super(g);
    }

    private UUID usedInContinuous;

    @Override
    public boolean startsWith(Model mo) {
        if (cstr.isContinuous()) {
            Mapping map = mo.getMapping();
            for (UUID vm : vms) {
                if (map.getRunningVMs().contains(vm)) {
                    if (usedInContinuous == null) {
                        usedInContinuous = map.getVMLocation(vm);
                    } else if (!usedInContinuous.equals(map.getVMLocation(vm))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        if (cstr.isContinuous() && vms.contains(a.getVM())) {
            if (usedInContinuous != null && a.getDestinationNode() != usedInContinuous) {
                return false;
            } else if (usedInContinuous == null) {
                usedInContinuous = a.getDestinationNode();
            }
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        UUID used = null;
        Mapping map = mo.getMapping();
        for (UUID vm : vms) {
            if (map.getRunningVMs().contains(vm)) {
                if (used == null) {
                    used = map.getVMLocation(vm);
                } else if (!used.equals(map.getVMLocation(vm))) {
                    return false;
                }
            }
        }
        return true;
    }
}
