package btrplace.model.constraint.checker;

import btrplace.model.constraint.Root;
import btrplace.plan.event.MigrateVM;

/**
 * Checker for the constraint.
 */
public class RootChecker extends AllowAllConstraintChecker {

    public RootChecker(Root r) {
        super(r);
    }

    @Override
    public boolean start(MigrateVM a) {
        return !vms.contains(a.getVM());
    }
}
