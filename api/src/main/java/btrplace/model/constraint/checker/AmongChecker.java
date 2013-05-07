package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Among;
import btrplace.plan.event.RunningVMPlacement;

import java.util.Set;
import java.util.UUID;

/**
 * Checker for the {@link Among} constraint
 *
 * @author Fabien Hermenier
 * @see Among
 */
public class AmongChecker extends AllowAllConstraintChecker<Among> {

    /**
     * Current group (for the continuous restriction). {@code null} if no group has been selected.
     */
    private Set<UUID> choosedGroup = null;

    /**
     * Make a new checker.
     *
     * @param a the associated constraint
     */
    public AmongChecker(Among a) {
        super(a);
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            Mapping map = mo.getMapping();
            for (UUID vm : getVMs()) {
                if (map.getRunningVMs().contains(vm)) {
                    Set<UUID> nodes = getConstraint().getAssociatedPGroup((map.getVMLocation(vm)));
                    if (nodes == null) {
                        return false;
                    } else if (choosedGroup == null) {
                        choosedGroup = nodes;
                    } else if (!choosedGroup.equals(nodes)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        if (getConstraint().isContinuous() && getVMs().contains(a.getVM())) {
            if (choosedGroup == null) {
                choosedGroup = getConstraint().getAssociatedPGroup(a.getDestinationNode());
                if (choosedGroup == null) {
                    //disallowed group
                    return false;
                }
            } else {
                if (!choosedGroup.contains(a.getDestinationNode())) {
                    //Not the right group
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean endsWith(Model i) {
        Mapping map = i.getMapping();
        Set<UUID> grp = null;
        for (UUID vm : getVMs()) {
            if (map.getRunningVMs().contains(vm)) {
                Set<UUID> nodes = getConstraint().getAssociatedPGroup((map.getVMLocation(vm)));
                if (nodes == null) {
                    return false;
                } else if (grp == null) {
                    grp = nodes;
                } else if (!grp.equals(nodes)) {
                    return false;
                }
            }
        }
        return true;
    }
}
