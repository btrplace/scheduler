package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Offline;
import btrplace.plan.event.BootNode;

import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Offline} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Offline
 */
public class OfflineChecker extends AllowAllConstraintChecker<Offline> {

    /**
     * Make a new checker.
     *
     * @param o the associated constraint
     */
    public OfflineChecker(Offline o) {
        super(o);
    }

    @Override
    public boolean start(BootNode a) {
        return !getNodes().contains(a.getNode());
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (UUID n : getNodes()) {
            if (!c.getOfflineNodes().contains(n)) {
                return false;
            }
        }
        return true;
    }
}
