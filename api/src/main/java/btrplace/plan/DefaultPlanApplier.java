package btrplace.plan;

import btrplace.plan.event.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A skeleton for {@link ReconfigurationPlanApplier} that provide the material
 * to propagate the notifications related to the termination of the actions.
 *
 * @author Fabien Hermenier
 */
public abstract class DefaultPlanApplier implements ReconfigurationPlanApplier, ActionVisitor {

    private List<EventCommittedListener> listeners;

    /**
     * Make a new applier.
     */
    public DefaultPlanApplier() {
        listeners = new ArrayList<EventCommittedListener>();
    }

    @Override
    public void addEventCommittedListener(EventCommittedListener l) {
        this.listeners.add(l);
    }

    @Override
    public boolean removeEventCommittedListener(EventCommittedListener l) {
        return this.listeners.remove(l);
    }

    /**
     * Propagate the event to every listener added by
     * {@link #addEventCommittedListener(EventCommittedListener)}.
     *
     * @param a the event to propagate
     */
    protected void fireAction(Action a) {
        for (EventCommittedListener l : listeners) {
            a.visit(this);
        }
    }

    protected void fireEvent(Event e) {
        for (EventCommittedListener l : listeners) {
            e.visit(this);
        }
    }


    @Override
    public Object visit(SuspendVM a) {
        fireAction(a);
        return Boolean.TRUE;
    }

    @Override
    public Object visit(Allocate a) {
        fireAction(a);
        return Boolean.TRUE;
    }

    @Override
    public Object visit(AllocateEvent a) {
        fireEvent(a);
        return Boolean.TRUE;
    }

    @Override
    public Object visit(SubstitutedVMEvent a) {
        fireEvent(a);
        return Boolean.TRUE;
    }

    @Override
    public Object visit(BootNode a) {
        fireAction(a);
        return Boolean.TRUE;
    }

    @Override
    public Object visit(BootVM a) {
        fireAction(a);
        return Boolean.TRUE;
    }

    @Override
    public Object visit(ForgeVM a) {
        fireAction(a);
        return Boolean.TRUE;
    }

    @Override
    public Object visit(KillVM a) {
        fireAction(a);
        return Boolean.TRUE;
    }

    @Override
    public Object visit(MigrateVM a) {
        fireAction(a);
        return Boolean.TRUE;
    }

    @Override
    public Object visit(ResumeVM a) {
        fireAction(a);
        return Boolean.TRUE;
    }

    @Override
    public Object visit(ShutdownNode a) {
        fireAction(a);
        return Boolean.TRUE;
    }

    @Override
    public Object visit(ShutdownVM a) {
        fireAction(a);
        return Boolean.TRUE;
    }
}
