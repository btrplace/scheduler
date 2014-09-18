package btrplace.model.constraint;

import btrplace.plan.event.*;

/**
 * Created by vkherbac on 01/09/14.
 */
public class NoDelayChecker extends AllowAllConstraintChecker<NoDelay> {
    /**
     * Make a new checker.
     *
     * @param nd the constraint associated to the checker.
     */
    public NoDelayChecker(NoDelay nd) {
        super(nd);
    }

    @Override
    public boolean start(MigrateVM a) {
        if(getVMs().contains(a.getVM())) {
            if (a.getStart() == 0) {
                return startRunningVMPlacement(a);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean start(BootVM a) {
        if(getVMs().contains(a.getVM())) {
            if (a.getStart() == 0) {
                return startRunningVMPlacement(a);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean start(ShutdownVM a) {
        if(getVMs().contains(a.getVM())) {
            return (a.getStart() == 0);
        }
        return true;
    }

    @Override
    public boolean start(ResumeVM a) {
        if(getVMs().contains(a.getVM())) {
            if (a.getStart() == 0) {
                return startRunningVMPlacement(a);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean start(SuspendVM a) {
        if(getVMs().contains(a.getVM())) {
            return (a.getStart() == 0);
        }
        return true;
    }

    @Override
    public boolean start(KillVM a) {
        if(getVMs().contains(a.getVM())) {
            return (a.getStart() == 0);
        }
        return true;
    }

    @Override
    public boolean start(ForgeVM a) {
        if(getVMs().contains(a.getVM())) {
            return (a.getStart() == 0);
        }
        return true;
    }
}
