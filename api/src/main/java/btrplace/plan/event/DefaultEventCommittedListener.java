package btrplace.plan.event;

import btrplace.plan.EventCommittedListener;

/**
 * Default implementation for {@link EventCommittedListener}.
 * Each of the committed action is redirected to {@link #committedAction(Action)}.
 * Each of the committed event is redirected to {@link #committedEvent(Event)}
 * These two methods do nothing.
 *
 * @author Fabien Hermenier
 */
public class DefaultEventCommittedListener implements EventCommittedListener {

    @Override
    public void committed(Allocate a) {
        committedAction(a);
    }

    @Override
    public void committed(AllocateEvent a) {
        committedEvent(a);
    }

    @Override
    public void committed(SubstitutedVMEvent a) {
        committedEvent(a);
    }

    @Override
    public void committed(BootNode a) {
        committedAction(a);
    }

    @Override
    public void committed(BootVM a) {
        committedAction(a);
    }

    @Override
    public void committed(ForgeVM a) {
        committedAction(a);
    }

    @Override
    public void committed(KillVM a) {
        committedAction(a);
    }

    @Override
    public void committed(MigrateVM a) {
        committedAction(a);
    }

    @Override
    public void committed(ResumeVM a) {
        committedAction(a);
    }

    @Override
    public void committed(ShutdownNode a) {
        committedAction(a);
    }

    @Override
    public void committed(ShutdownVM a) {
        committedAction(a);
    }

    @Override
    public void committed(SuspendVM a) {
        committedAction(a);
    }

    /**
     * An action was committed.
     *
     * @param a the committed action
     */
    public void committedAction(Action a) {
    }

    /**
     * An event was committed
     *
     * @param e the committed event
     */
    public void committedEvent(Event e) {
    }
}
