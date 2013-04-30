package btrplace.model.constraint.checker;

import btrplace.model.Model;
import btrplace.model.constraint.Preserve;
import btrplace.model.view.ShareableResource;
import btrplace.plan.event.Allocate;
import btrplace.plan.event.AllocateEvent;

import java.util.UUID;

/**
 * Checker for the constraint.
 */
public class PreserveChecker extends AllowAllConstraintChecker {

    private int amount;
    private String id;

    public PreserveChecker(Preserve p) {
        super(p);
        id = p.getResource();
        amount = p.getAmount();
    }

    @Override
    public boolean consume(AllocateEvent a) {
        if (vms.contains(a.getVM()) && a.getResourceId().equals(id)) {
            return a.getAmount() >= amount;
        }
        return true;
    }

    @Override
    public boolean start(Allocate a) {
        if (a.getResourceId().equals(id) && vms.contains(a.getVM())) {
            return a.getAmount() >= amount;
        }
        return true;
    }

    @Override
    public boolean startsWith(Model mo) {
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        ShareableResource r = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE + id);
        if (r == null) {
            return false;
        }
        for (UUID vmId : vms) {
            int v = r.get(vmId);
            if (v < amount) {
                return false;
            }
        }
        return true;
    }
}
