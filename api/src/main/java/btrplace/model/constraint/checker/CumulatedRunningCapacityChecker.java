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
public class CumulatedRunningCapacityChecker extends AllowAllConstraintChecker {

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
        if (cstr.isContinuous() && nodes.contains(n)) {
            usage--;
        }
        return true;
    }

    private boolean arrive(UUID n) {
        if (cstr.isContinuous() && nodes.contains(n)) {
            if (usage++ == qty) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean start(BootVM a) {
        return arrive(a.getDestinationNode());
    }

    @Override
    public boolean start(KillVM a) {
        if (cstr.isContinuous() && srcRunnings.remove(a.getVM())) {
            return leave(a.getNode());
        }
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        if (cstr.isContinuous()) {
            if (!(nodes.contains(a.getSourceNode()) && nodes.contains(a.getDestinationNode()))) {
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
    public boolean consume(SubstitutedVMEvent e) {
        return super.consume(e);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean startsWith(Model mo) {
        if (cstr.isContinuous()) {
            int nb = 0;
            Mapping map = mo.getMapping();
            for (UUID n : nodes) {
                nb += map.getRunningVMs(n).size();
                if (nb > qty) {
                    return false;
                }
            }
            srcRunnings = new HashSet<>(map.getRunningVMs());
        }
        return true;
    }

    @Override
    public boolean endsWith(Model mo) {
        int nb = 0;
        Mapping map = mo.getMapping();
        for (UUID n : nodes) {
            nb += map.getRunningVMs(n).size();
            if (nb > qty) {
                return false;
            }
        }
        return true;
    }
}
