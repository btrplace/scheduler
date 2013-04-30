package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Offline;
import btrplace.plan.event.BootNode;

import java.util.UUID;

/**
 * @author Fabien Hermenier
 */
public class OfflineChecker extends AllowAllConstraintChecker {

    public OfflineChecker(Offline o) {
        super(o);
    }

    @Override
    public boolean start(BootNode a) {
        return !nodes.contains(a.getNode());
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (UUID n : nodes) {
            if (!c.getOfflineNodes().contains(n)) {
                return false;
            }
        }
        return true;
    }
}
