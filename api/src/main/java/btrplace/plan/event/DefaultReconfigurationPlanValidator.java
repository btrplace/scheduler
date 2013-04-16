package btrplace.plan.event;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlanValidator;

import java.util.Set;
import java.util.UUID;

/**
 * Default implementation of {@link ReconfigurationPlanValidator}.
 * Every {@code accept*} method returns {@code true}.
 * <p/>
 * This implementation tracks {@link SubstitutedVMEvent}.
 * If a set of VMs to track is given at instantiation, the class
 * will update this set according to the received {@link SubstitutedVMEvent}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlanValidator implements ReconfigurationPlanValidator {

    private Set<UUID> trackedVMs;

    public DefaultReconfigurationPlanValidator(Set<UUID> myVMs) {
        this.trackedVMs = myVMs;
    }

    /**
     * Get the current set of tracked VMs
     *
     * @return a set of UUIDs.
     */
    public Set<UUID> getTrackedVMs() {
        return this.trackedVMs;
    }

    /**
     * Check if a VM is tracked.
     *
     * @param vm the vm identifier
     * @return {@code true} iff the VM is tracked
     */
    public boolean isTracked(UUID vm) {
        return this.trackedVMs.contains(vm);
    }

    @Override
    public boolean accept(Allocate a) {
        return true;
    }


    @Override
    public boolean accept(AllocateEvent a) {
        return true;
    }

    @Override
    public boolean accept(SubstitutedVMEvent a) {
        if (trackedVMs.remove(a.getVM())) {
            return trackedVMs.add(a.getNewUUID());
        }
        return true;
    }


    @Override
    public boolean accept(BootNode a) {
        return true;
    }


    @Override
    public boolean accept(BootVM a) {
        return true;
    }


    @Override
    public boolean accept(ForgeVM a) {
        return true;
    }


    @Override
    public boolean accept(KillVM a) {
        return true;
    }


    @Override
    public boolean accept(MigrateVM a) {
        return true;
    }


    @Override
    public boolean accept(ResumeVM a) {
        return true;
    }


    @Override
    public boolean accept(ShutdownNode a) {
        return true;
    }


    @Override
    public boolean accept(ShutdownVM a) {
        return true;
    }


    @Override
    public boolean accept(SuspendVM a) {
        return true;
    }

    @Override
    public boolean accept(Model mo) {
        return true;
    }
}
