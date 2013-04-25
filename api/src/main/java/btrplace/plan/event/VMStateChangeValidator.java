package btrplace.plan.event;

import btrplace.model.Model;

import java.util.Set;
import java.util.UUID;

/**
 * @author Fabien Hermenier
 */
public abstract class VMStateChangeValidator extends DefaultReconfigurationPlanValidator {


    public VMStateChangeValidator(Set<UUID> vms) {
        super(vms);
    }

    @Override
    public boolean accept(BootVM a) {
        return !isTracked(a.getVM());
    }

    @Override
    public boolean accept(ForgeVM a) {
        return !isTracked(a.getVM());
    }

    @Override
    public boolean accept(KillVM a) {
        return !isTracked(a.getVM());
    }

    @Override
    public boolean accept(MigrateVM a) {
        return !isTracked(a.getVM());
    }

    @Override
    public boolean accept(ResumeVM a) {
        return !isTracked(a.getVM());
    }

    @Override
    public boolean accept(ShutdownVM a) {
        return !isTracked(a.getVM());
    }

    @Override
    public boolean accept(SuspendVM a) {
        return !isTracked(a.getVM());
    }

    @Override
    public boolean acceptResultingModel(Model mo) {
        return false;
    }

    @Override
    public boolean acceptOriginModel(Model mo) {
        return true;
    }
}
