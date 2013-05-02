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

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SequentialVMTransitionsChecker(SequentialVMTransitions s) {
        super(s);
        order = new ArrayList<>(s.getInvolvedVMs());
    }

    private boolean wasNext(UUID vm) {
        if (getVMs().contains(vm)) {
            //Everything before vm is considered as terminated

            while (!order.isEmpty() && !order.get(0).equals(vm)) {
                order.remove(0);
            }
            if (order.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void updateOrder(UUID vm) {
        if (getVMs().contains(vm)) {
            //Everything before vm is supposed to be terminated
            while (!order.isEmpty()) {
                if (order.remove(0).equals(vm)) {
                    break;
                }
            }
        }
    }

    @Override
    public boolean startsWith(Model mo) {
        runnings = new HashSet<>(mo.getMapping().getRunningVMs());
        track(runnings);
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        //If the VM belong to the order we remove it
        order.remove(a.getVM());
        return true;
    }

    @Override
    public void end(BootVM a) {
        updateOrder(a.getVM());
    }

    @Override
    public void end(MigrateVM a) {
    }

    @Override
    public void end(ShutdownVM a) {
        updateOrder(a.getVM());
    }

    @Override
    public void end(SuspendVM a) {
        updateOrder(a.getVM());
    }

    @Override
    public void end(ResumeVM a) {
        updateOrder(a.getVM());
    }

    @Override
    public void end(ForgeVM a) {
        updateOrder(a.getVM());
    }

    @Override
    public void end(KillVM a) {
        updateOrder(a.getVM());
    }

    @Override
    public boolean start(ShutdownVM a) {
        return wasNext(a.getVM());
    }

    @Override
    public boolean start(SuspendVM a) {
        return wasNext(a.getVM());
    }

    @Override
    public boolean start(KillVM a) {
        return runnings.contains(a.getVM()) && wasNext(a.getVM());
    }

    @Override
    public boolean start(ForgeVM a) {
        return super.start(a);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        return wasNext(a.getVM());
    }

    @Override
    public void endRunningVMPlacement(RunningVMPlacement a) {
        updateOrder(a.getVM());
    }

}
