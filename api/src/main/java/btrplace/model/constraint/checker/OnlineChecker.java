package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Online;
import btrplace.plan.event.ShutdownNode;

import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Online} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Online
 */
public class OnlineChecker extends AllowAllConstraintChecker {

    /**
     * Make a new checker.
     *
     * @param o the associated constraint
     */
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
