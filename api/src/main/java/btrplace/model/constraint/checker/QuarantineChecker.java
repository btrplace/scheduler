package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Quarantine;
import btrplace.plan.event.RunningVMPlacement;
import btrplace.plan.event.MigrateVM;

/**
 * Checker for the {@link btrplace.model.constraint.Quarantine} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Quarantine
 */
public class QuarantineChecker extends AllowAllConstraintChecker<Quarantine> {

    /**
     * Make a new checker.
     *
     * @param q the associated constraint
     */
    public QuarantineChecker(Quarantine q) {
        super(q);
    }

    @Override
    public boolean start(MigrateVM a) {
        if (getVMs().contains(a.getVM())) { //the VM can not move elsewhere
            return false;
        }
        return startRunningVMPlacement(a);
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        return !getNodes().contains(a.getDestinationNode());
    }

    @Override
    public boolean endsWith(Model mo) {
        return true;
    }

    @Override
    public boolean startsWith(Model mo) {
        Mapping map = mo.getMapping();
        getVMs().clear();
        return getVMs().addAll(map.getRunningVMs(getNodes()));
    }

}
