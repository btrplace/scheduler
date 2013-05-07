package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Sleeping;
import btrplace.plan.event.SuspendVM;

import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Sleeping} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Sleeping
 */
public class SleepingChecker extends DenyMyVMsActions<Sleeping> {

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SleepingChecker(Sleeping s) {
        super(s);
    }

    @Override
    public boolean start(SuspendVM a) {
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (UUID vm : getVMs()) {
            if (!c.getSleepingVMs().contains(vm)) {
                return false;
            }
        }
        return true;
    }
}
