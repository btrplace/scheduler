package btrplace.model.constraint.checker;

import btrplace.model.Model;
import btrplace.model.constraint.CumulatedResourceCapacity;
import btrplace.model.view.ShareableResource;

import java.util.UUID;

/**
 * @author Fabien Hermenier
 */
public class CumulatedResourceCapacityChecker extends AllowAllConstraintChecker {

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
