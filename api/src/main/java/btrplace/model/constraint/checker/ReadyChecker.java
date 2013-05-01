package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Ready;
import btrplace.plan.event.ForgeVM;
import btrplace.plan.event.ShutdownVM;

import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Ready} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Ready
 */
public class ReadyChecker extends DenyMyVMsActions {

    /**
     * Make a new checker.
     *
     * @param r the associated constraint
     */
    public ReadyChecker(Ready r) {
        super(r);
    }

    @Override
    public boolean start(ForgeVM a) {
        return true;
    }

    @Override
    public boolean start(ShutdownVM a) {
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (UUID vm : vms) {
            if (!c.getReadyVMs().contains(vm)) {
                return false;
            }
        }
        return true;
    }
}
