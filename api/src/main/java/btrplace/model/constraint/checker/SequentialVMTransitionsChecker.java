package btrplace.model.constraint.checker;

import btrplace.model.Model;
import btrplace.model.constraint.SequentialVMTransitions;
import btrplace.plan.event.*;

import java.util.*;

/**
 * TODO: make it resilient to substitution.
 */
public class SequentialVMTransitionsChecker extends AllowAllConstraintChecker {

    private Set<UUID> runnings;

    private List<UUID> order;

    public SequentialVMTransitionsChecker(SequentialVMTransitions s) {
        super(s);
        order = new ArrayList<>(s.getInvolvedVMs());
    }

    @Override
    public boolean start(BootVM a) {
        if (runnings.contains(a.getVM())) {
            return wasNext(a.getVM());
        }
        return true;
    }

    @Override
    public boolean startsWith(Model mo) {
        runnings = new HashSet<>(mo.getMapping().getRunningVMs());
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        //If the VM belong to the order we remove it
        order.remove(a.getVM());
        return true;
    }

    private boolean wasNext(UUID vm) {
        if (vms.contains(vm)) {
            while (!order.isEmpty()) {
                if (order.remove(0).equals(vm)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean start(ShutdownVM a) {
        return wasNext(a.getVM());
    }

    @Override
    public boolean start(ResumeVM a) {
        return wasNext(a.getVM());
    }

    @Override
    public boolean start(SuspendVM a) {
        return wasNext(a.getVM());
    }

    @Override
    public boolean start(KillVM a) {
        //TODO: only if was not ready
        return wasNext(a.getVM());
    }
}
