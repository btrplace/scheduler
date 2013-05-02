package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Killed;
import btrplace.plan.event.KillVM;

import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Killed} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Killed
 */
public class KilledChecker extends DenyMyVMsActions<Killed> {

    /**
     * Make a new checker.
     *
     * @param k the associated constraint
     */
    public KilledChecker(Killed k) {
        super(k);
    }

    @Override
    public boolean start(KillVM a) {
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping c = mo.getMapping();
        for (UUID vm : vms) {
            if (c.getAllVMs().contains(vm)) {
                return false;
            }
        }
        return true;
    }
}
