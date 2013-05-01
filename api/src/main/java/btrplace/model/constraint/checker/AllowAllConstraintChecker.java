package btrplace.model.constraint.checker;

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.plan.RunningVMPlacement;
import btrplace.plan.event.*;

import java.util.*;

/**
 * A default constraint checker that allow every action and event.
 * In addition, {@link SubstitutedVMEvent} events are tracked and
 * considered to maintain the set of VMs that is involved in
 * the constraint.
 *
 * @author Fabien Hermenier
 */
public abstract class AllowAllConstraintChecker implements SatConstraintChecker {

    /**
     * VMs involved in the constraint.
     * Updated after each {@link btrplace.plan.event.SubstitutedVMEvent} event.
     */
    protected Set<UUID> vms;

    /**
     * Nodes involved in the constraint.
     */
    protected Set<UUID> nodes;

    protected SatConstraint cstr;

    private List<Collection<UUID>> tracked;

    /**
     * Make a new checker.
     *
     * @param cstr the constraint associated to the checker.
     */
    public AllowAllConstraintChecker(SatConstraint cstr) {
        this.vms = new HashSet<>(cstr.getInvolvedVMs());
        this.nodes = new HashSet<>(cstr.getInvolvedNodes());
        this.cstr = cstr;
        tracked = new ArrayList<>();
    }

    /**
     * Register a new set of VMs UUID to track.
     * Each {@link SubstitutedVMEvent} event is catched
     * and all of the registered collections are updated
     * accordingly
     *
     * @param c the collection to register
     * @return {@code true} iff the collection has been added
     */
    public boolean track(Collection<UUID> c) {
        return tracked.add(c);
    }

    @Override
    public boolean startsWith(Model mo) {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @param a the executed that will be executed
     * @return {@code startRunningVMPlacement(a)}
     */
    @Override
    public boolean start(MigrateVM a) {
        return startRunningVMPlacement(a);
    }

    /**
     * {@inheritDoc}
     *
     * @param a the executed that will be executed
     * @return {@code endRunningVMPlacement(a)}
     */
    @Override
    public void end(MigrateVM a) {
        endRunningVMPlacement(a);
    }

    /**
     * {@inheritDoc}
     *
     * @param a the executed that will be executed
     * @return {@code startRunningVMPlacement(a)}
     */
    @Override
    public boolean start(BootVM a) {
        return startRunningVMPlacement(a);
    }

    /**
     * {@inheritDoc}
     *
     * @param a the executed that will be executed
     * @return {@code endRunningVMPlacement(a)}
     */
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
        return startRunningVMPlacement(a);
    }

    @Override
    public void end(ResumeVM a) {
        endRunningVMPlacement(a);
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
        for (Collection<UUID> c : tracked) {
            if (c.remove(e.getVM())) {
                c.add(e.getNewUUID());
            }
        }
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
