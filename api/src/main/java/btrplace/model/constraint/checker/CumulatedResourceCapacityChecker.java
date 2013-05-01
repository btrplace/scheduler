package btrplace.model.constraint.checker;

import btrplace.model.Model;
import btrplace.model.constraint.CumulatedResourceCapacity;
import btrplace.model.view.ShareableResource;

import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.CumulatedResourceCapacity} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.CumulatedResourceCapacity
 */
public class CumulatedResourceCapacityChecker extends AllowAllConstraintChecker {

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public CumulatedResourceCapacityChecker(CumulatedResourceCapacity s) {
        super(s);
    }

    @Override
    public boolean endsWith(Model i) {
        ShareableResource rc = (ShareableResource) i.getView(ShareableResource.VIEW_ID_BASE + ((CumulatedResourceCapacity) cstr).getResource());
        if (rc == null) {
            return false;
        }

        int remainder = ((CumulatedResourceCapacity) cstr).getAmount();
        for (UUID id : nodes) {
            if (i.getMapping().getOnlineNodes().contains(id)) {
                for (UUID vmId : i.getMapping().getRunningVMs(id)) {
                    remainder -= rc.get(vmId);
                    if (remainder < 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
