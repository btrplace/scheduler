package btrplace.model.constraint.checker;

import btrplace.model.SatConstraint;
import btrplace.plan.RunningVMPlacement;
import btrplace.plan.event.*;

/**
 * Basic checker that allow everything except all the actions on my VMs.
 *
 * @author Fabien Hermenier
 */
public abstract class DenyMyVMsActions<C extends SatConstraint> extends AllowAllConstraintChecker<C> {

    /**
     * New instance.
     *
     * @param s the constraint associated to the checker.
     */
    public DenyMyVMsActions(C s) {
        super(s);
    }

    @Override
    public boolean start(ShutdownVM a) {
        return !getVMs().contains(a.getVM());
    }

    @Override
    public boolean start(SuspendVM a) {
        return !getVMs().contains(a.getVM());
    }

    @Override
    public boolean start(KillVM a) {
        return !getVMs().contains(a.getVM());
    }

    @Override
    public boolean start(Allocate a) {
        return !getVMs().contains(a.getVM());
    }

    @Override
    public boolean consume(SubstitutedVMEvent e) {
        return !getVMs().contains(e.getVM());
    }

    @Override
    public boolean consume(AllocateEvent e) {
        return !getVMs().contains(e.getVM());
    }

    @Override
    public boolean start(ForgeVM a) {
        return !getVMs().contains(a.getVM());
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        return !getVMs().contains(a.getVM());
    }
}
