package btrplace.model.constraint.checker;

import btrplace.model.constraint.Root;
import btrplace.plan.event.MigrateVM;

/**
 * Checker for the {@link btrplace.model.constraint.Root} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Root
 */
public class RootChecker extends AllowAllConstraintChecker {

    /**
     * Make a new checker.
     *
     * @param r the associated constraint
     */
    public RootChecker(Root r) {
        super(r);
    }

    @Override
    public boolean start(MigrateVM a) {
        return !vms.contains(a.getVM());
    }
}
