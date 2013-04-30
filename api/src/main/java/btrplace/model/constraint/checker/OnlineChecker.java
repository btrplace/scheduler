package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Online;
import btrplace.plan.event.ShutdownNode;

import java.util.UUID;

/**
 * @author Fabien Hermenier
 */
public class OnlineChecker extends AllowAllConstraintChecker {

    public OnlineChecker(Online o) {
        super(o);
    }

    @Override
    public boolean start(ShutdownNode a) {
        return !nodes.contains(a.getNode());
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (UUID n : nodes) {
            if (!c.getOnlineNodes().contains(n)) {
                return false;
            }
        }
        return true;
    }
}
