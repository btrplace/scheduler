package btrplace.plan;

import btrplace.plan.event.NotificationDispatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * A skeleton for {@link ReconfigurationPlanApplier}.
 * It provides that provide the material
 * to propagate the notifications related to the termination of the actions.
 *
 * @author Fabien Hermenier
 */
public abstract class DefaultPlanApplier implements ReconfigurationPlanApplier {

    private List<EventCommittedListener> listeners;

    private NotificationDispatcher notificationDispatcher;

    /**
     * Make a new applier.
     */
    public DefaultPlanApplier() {
        listeners = new ArrayList<>();
        notificationDispatcher = new NotificationDispatcher(listeners);
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
     * Propagate the action to every listener added by
     * {@link #addEventCommittedListener(EventCommittedListener)}.
     * Events hooked on {@link btrplace.plan.Action.Hook#pre} are propagated in first
     * Then the real action is propagated. Finally, events hooked on {@link btrplace.plan.Action.Hook#post}
     * are propagated
     *
     * @param a the event to propagate
     */
    public void fireAction(Action a) {

        for (Event e : a.getEvents(Action.Hook.pre)) {
            e.visit(notificationDispatcher);
        }
        a.visit(notificationDispatcher);

        for (Event e : a.getEvents(Action.Hook.post)) {
            e.visit(notificationDispatcher);
        }
    }

}
