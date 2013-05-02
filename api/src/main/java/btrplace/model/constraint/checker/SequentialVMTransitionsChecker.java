package btrplace.model.constraint.checker;

import btrplace.model.Model;
import btrplace.model.constraint.SequentialVMTransitions;
import btrplace.plan.RunningVMPlacement;
import btrplace.plan.event.*;

import java.util.*;

/**
 * Checker for the {@link btrplace.model.constraint.SequentialVMTransitions} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.SequentialVMTransitions
 */
public class SequentialVMTransitionsChecker extends AllowAllConstraintChecker<SequentialVMTransitions> {

    private Set<UUID> runnings;

    private List<UUID> order;

    private UUID pending;

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SequentialVMTransitionsChecker(SequentialVMTransitions s) {
        super(s);
        order = new ArrayList<>(s.getInvolvedVMs());
    }

    @Override
    public boolean startsWith(Model mo) {
        runnings = new HashSet<>(mo.getMapping().getRunningVMs());
        track(runnings);
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        return true;
    }

    private boolean makePending(UUID vm) {
        if (getVMs().contains(vm)) {
            if (pending == null) {
                //Burn all the VMs in order that are before vm
                while (!order.isEmpty() && !order.get(0).equals(vm)) {
                    order.remove(0);
                }
                if (order.isEmpty()) {
                    return false;
                }
                pending = vm;
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean start(BootVM a) {
        return makePending(a.getVM());
    }

    @Override
    public boolean start(ShutdownVM a) {
        return makePending(a.getVM());
    }

    @Override
    public boolean start(ResumeVM a) {
        return makePending(a.getVM());
    }

    @Override
    public boolean start(SuspendVM a) {
        return makePending(a.getVM());
    }

    @Override
    public boolean start(KillVM a) {
        if (runnings.contains(a.getVM())) {
            return makePending(a.getVM());
        }
        return true;
    }

    @Override
    public boolean start(ForgeVM a) {
        return makePending(a.getVM());
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        return makePending(a.getVM());
    }

    @Override
    public void end(BootVM a) {
        if (a.getVM().equals(pending)) {
            pending = null;
        }
    }

    @Override
    public void end(ShutdownVM a) {
        if (a.getVM().equals(pending)) {
            pending = null;
        }
    }

    @Override
    public void end(ResumeVM a) {
        if (a.getVM().equals(pending)) {
            pending = null;
        }

    }

    @Override
    public void end(SuspendVM a) {
        if (a.getVM().equals(pending)) {
            pending = null;
        }

    }

    @Override
    public void end(KillVM a) {
        if (a.getVM().equals(pending)) {
            pending = null;
        }
    }

    @Override
    public void end(ForgeVM a) {
        if (a.getVM().equals(pending)) {
            pending = null;
        }
    }
}
