package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Among;

import java.util.Set;
import java.util.UUID;

/**
 * @author Fabien Hermenier
 */
public class AmongChecker extends AllowAllConstraintChecker {

    public AmongChecker(Among a) {
        super(a);
    }

    @Override
    public boolean endsWith(Model i) {
        Mapping map = i.getMapping();
        Set<UUID> choosedGroup = null;
        for (UUID vm : vms) {
            if (map.getRunningVMs().contains(vm)) {
                Set<UUID> nodes = ((Among) cstr).getAssociatedPGroup((map.getVMLocation(vm)));
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
