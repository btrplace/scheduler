package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.SingleResourceCapacity;
import btrplace.model.view.ShareableResource;

import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.SingleResourceCapacity} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.SingleResourceCapacity
 */
public class SingleResourceCapacityChecker extends AllowAllConstraintChecker<SingleResourceCapacity> {

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SingleResourceCapacityChecker(SingleResourceCapacity s) {
        super(s);
    }

    @Override
    public boolean endsWith(Model i) {
        ShareableResource rc = (ShareableResource) i.getView(ShareableResource.VIEW_ID_BASE + getConstraint().getResource());
        if (rc == null) {
            return false;
        }
        Mapping map = i.getMapping();
        for (UUID n : getNodes()) {
            if (rc.sum(map.getRunningVMs(n), true) > getConstraint().getAmount()) {
                return false;
            }
        }
        return true;
    }
}
