package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.CumulatedRunningCapacity;
import btrplace.plan.event.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Checker for the {@link btrplace.model.constraint.CumulatedRunningCapacity} constraint
 *
 * @author Fabien Hermenier
 * @see btrplace.model.constraint.CumulatedRunningCapacity
 */
public class CumulatedRunningCapacityChecker extends AllowAllConstraintChecker<CumulatedRunningCapacity> {

    private int usage;

    private Set<UUID> srcRunnings;

    private int qty;

    /**
     * Make a new checker.
     *
     * @param c the associated constraint
     */
    public CumulatedRunningCapacityChecker(CumulatedRunningCapacity c) {
        super(c);
        qty = c.getAmount();
    }

    private boolean leave(UUID n) {
        if (getConstraint().isContinuous() && getNodes().contains(n)) {
            usage--;
        }
        return true;
    }

    private boolean arrive(UUID n) {
        return !(getConstraint().isContinuous() && getNodes().contains(n) && usage++ == qty);
    }

    @Override
    public boolean start(BootVM a) {
        return arrive(a.getDestinationNode());
    }

    @Override
    public boolean start(KillVM a) {
        if (getConstraint().isContinuous() && srcRunnings.remove(a.getVM())) {
            return leave(a.getNode());
        }
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        if (getConstraint().isContinuous()) {
            if (!(getNodes().contains(a.getSourceNode()) && getNodes().contains(a.getDestinationNode()))) {
                return leave(a.getSourceNode()) && arrive(a.getDestinationNode());
            }
        }
        return true;
    }

    @Override
    public boolean start(ResumeVM a) {
        return arrive(a.getDestinationNode());
    }

    @Override
    public boolean start(ShutdownVM a) {
        return leave(a.getNode());
    }

    @Override
    public boolean start(SuspendVM a) {
        return leave(a.getSourceNode());
    }

    @Override
    public boolean startsWith(Model mo) {
        if (getConstraint().isContinuous()) {
            int nb = 0;
            Mapping map = mo.getMapping();
            for (UUID n : getNodes()) {
                nb += map.getRunningVMs(n).size();
                if (nb > qty) {
                    return false;
                }
            }
            srcRunnings = new HashSet<>(map.getRunningVMs());
            track(srcRunnings);
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        int nb = 0;
        Mapping map = mo.getMapping();
        for (UUID n : getNodes()) {
            nb += map.getRunningVMs(n).size();
            if (nb > qty) {
                return false;
            }
        }
        return true;
    }
}
