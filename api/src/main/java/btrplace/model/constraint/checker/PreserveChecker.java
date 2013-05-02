package btrplace.model.constraint.checker;

import btrplace.model.Model;
import btrplace.model.constraint.Preserve;
import btrplace.model.view.ShareableResource;
import btrplace.plan.event.Allocate;
import btrplace.plan.event.AllocateEvent;

import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Preserve} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Preserve
 */
public class PreserveChecker extends AllowAllConstraintChecker<Preserve> {

    private int amount;

    private String id;

    /**
     * Make a new checker.
     *
     * @param p the associated constraint
     */
    public PreserveChecker(Preserve p) {
        super(p);
        id = p.getResource();
        amount = p.getAmount();
    }

    @Override
    public boolean consume(AllocateEvent a) {
        if (getVMs().contains(a.getVM()) && a.getResourceId().equals(id)) {
            return a.getAmount() >= amount;
        }
        return true;
    }

    @Override
    public boolean start(Allocate a) {
        if (a.getResourceId().equals(id) && getVMs().contains(a.getVM())) {
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
        for (UUID vmId : getVMs()) {
            int v = r.get(vmId);
            if (v < amount) {
                return false;
            }
        }
        return true;
    }
}
