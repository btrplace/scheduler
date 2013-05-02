package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Among;

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
     * Make a new checker.
     *
     * @param a the associated constraint
     */
    public AmongChecker(Among a) {
        super(a);
    }

    @Override
    public boolean endsWith(Model i) {
        Mapping map = i.getMapping();
        Set<UUID> choosedGroup = null;
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
        return true;
    }
}
