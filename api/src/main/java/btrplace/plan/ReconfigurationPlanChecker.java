package btrplace.plan;

import btrplace.model.Model;
import btrplace.plan.event.*;

/**
 * Check for the validity of a reconfiguration plan.
 * Once started, actions are started wrt. their starting date, in
 * the increase order.
 * Actions that ends at a moment 't', are notified before actions that start
 * at the same moment.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlanChecker {

    boolean startsWith(Model mo);

    boolean start(MigrateVM a);
    void end(MigrateVM a);

    boolean start(BootVM a);
    void end(BootVM a);

    boolean start(BootNode a);
    void end(BootNode a);

    boolean start(ShutdownVM a);
    void end(ShutdownVM a);

    boolean start(ShutdownNode a);
    void end(ShutdownNode a);

    boolean start(ResumeVM a);
    void end(ResumeVM a);

    boolean start(SuspendVM a);
    void end(SuspendVM a);

    boolean start(KillVM a);
    void end(KillVM a);

    boolean start(ForgeVM a);
    void end(ForgeVM a);

    boolean start(SubstitutedVMEvent e);

    boolean start(AllocateEvent e);

    boolean start(Allocate e);
    void end(Allocate e);

    boolean endsWith(Model mo);
}
