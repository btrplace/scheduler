package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Spread;
import btrplace.plan.event.RunningVMPlacement;
import btrplace.plan.event.KillVM;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownVM;
import btrplace.plan.event.SuspendVM;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.Spread} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.Ban
 */
public class SpreadChecker extends AllowAllConstraintChecker<Spread> {

    /**
     * Make a new checker.
     *
     * @param s the associated constraint
     */
    public SpreadChecker(Spread s) {
        super(s);
        denied = new HashSet<>();
    }

    private Set<UUID> denied;

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            Mapping map = mo.getMapping();
            for (UUID vm : getVMs()) {
                UUID n = map.getVMLocation(vm);
                if (n != null) {
                    denied.add(n);
                }
            }
        }
        return true;
    }

    @Override
    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        if (getConstraint().isContinuous() && getVMs().contains(a.getVM())) {
            if (denied.contains(a.getDestinationNode())) {
                return false;
            }
            denied.add(a.getDestinationNode());
        }
        return true;
    }

    @Override
    public void end(MigrateVM a) {
        unDenied(a.getVM(), a.getSourceNode());
    }

    private void unDenied(UUID vm, UUID n) {
        if (getConstraint().isContinuous()) {
            if (getVMs().contains(vm)) {
                denied.remove(n);
            }
        }
    }

    @Override
    public void end(ShutdownVM a) {
        unDenied(a.getVM(), a.getNode());
    }

    @Override
    public void end(SuspendVM a) {
        unDenied(a.getVM(), a.getSourceNode());
    }

    @Override
    public void end(KillVM a) {
        unDenied(a.getVM(), a.getNode());
    }

    @Override
    public boolean endsWith(Model mo) {
        Set<UUID> forbidden = new HashSet<>();
        Mapping map = mo.getMapping();
        for (UUID vm : getVMs()) {
            if (map.getRunningVMs().contains(vm)) {
                if (!forbidden.add(map.getVMLocation(vm))) {
                    return false;
                }
            }
        }
        return true;
    }
}
