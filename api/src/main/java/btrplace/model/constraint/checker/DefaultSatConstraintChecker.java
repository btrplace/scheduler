package btrplace.model.constraint.checker;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.plan.RunningVMPlacement;
import btrplace.plan.event.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Fabien Hermenier
 */
public abstract class DefaultSatConstraintChecker implements SatConstraintChecker {

    protected Set<UUID> vms;

    protected Set<UUID> nodes;

    private SatConstraint cstr;

    public DefaultSatConstraintChecker(SatConstraint cstr) {
        this.vms = new HashSet<>(cstr.getInvolvedVMs());
        this.nodes = new HashSet<>(cstr.getInvolvedNodes());
        this.cstr = cstr;
    }

    @Override
    public boolean startsWith(Model mo) {
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        return startRunningVMPlacement(a);
    }

    @Override
    public void end(MigrateVM a) {
        endRunningVMPlacement(a);
    }

    @Override
    public boolean start(BootVM a) {
        return startRunningVMPlacement(a);
    }

    @Override
    public void end(BootVM a) {
        endRunningVMPlacement(a);
    }

    @Override
    public boolean start(BootNode a) {
        return true;
    }

    @Override
    public void end(BootNode a) {

    }

    @Override
    public boolean start(ShutdownVM a) {
        return true;
    }

    @Override
    public void end(ShutdownVM a) {

    }

    @Override
    public boolean start(ShutdownNode a) {
        return true;
    }

    @Override
    public void end(ShutdownNode a) {

    }

    @Override
    public boolean start(ResumeVM a) {
        return true;
    }

    @Override
    public void end(ResumeVM a) {

    }

    @Override
    public boolean start(SuspendVM a) {
        return true;
    }

    @Override
    public void end(SuspendVM a) {

    }

    @Override
    public boolean start(KillVM a) {
        return true;
    }

    @Override
    public void end(KillVM a) {

    }

    @Override
    public boolean start(ForgeVM a) {
        return true;
    }

    @Override
    public void end(ForgeVM a) {

    }

    @Override
    public boolean endsWith(Model mo) {
        return true;
    }

    @Override
    public boolean consume(SubstitutedVMEvent e) {
        return !vms.remove(e.getVM()) || vms.add(e.getNewUUID());
    }

    @Override
    public boolean consume(AllocateEvent e) {
        return true;
    }

    @Override
    public boolean start(Allocate e) {
        return true;
    }

    @Override
    public void end(Allocate e) {

    }

    public boolean startRunningVMPlacement(RunningVMPlacement a) {
        return true;
    }

    public void endRunningVMPlacement(RunningVMPlacement a) {

    }

    @Override
    public SatConstraint getConstraint() {
        return cstr;
    }
}
