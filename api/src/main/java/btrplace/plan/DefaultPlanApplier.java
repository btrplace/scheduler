package btrplace.plan;

import btrplace.plan.event.NotificationDispatcher;
import btrplace.plan.event.ValidatorDispatcher;

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

    private List<ReconfigurationPlanValidator> validators;

    private NotificationDispatcher notificationDispatcher;

    private ValidatorDispatcher validatorDispatcher;

    /**
     * Make a new applier.
     */
    public DefaultPlanApplier() {
        listeners = new ArrayList<>();
        validators = new ArrayList<>();
        notificationDispatcher = new NotificationDispatcher(listeners);
        validatorDispatcher = new ValidatorDispatcher(validators);
    }

    @Override
    public void addEventCommittedListener(EventCommittedListener l) {
        this.listeners.add(l);
    }

    @Override
    public boolean removeEventCommittedListener(EventCommittedListener l) {
        return this.listeners.remove(l);
    }

    @Override
    public void addValidator(ReconfigurationPlanValidator v) {
        this.validators.add(v);
    }

    @Override
    public boolean removeValidator(ReconfigurationPlanValidator v) {
        return validators.remove(v);
    }

    /**
     * Propagate the action to every listener added by
     * {@link #addEventCommittedListener(EventCommittedListener)}.
     * iff it is validated by all the validators declared by {@link #addValidator(ReconfigurationPlanValidator)}.
     * <p/>
     * Events hooked on {@link btrplace.plan.Action.Hook#pre} are propagated in first
     * Then the real action is propagated. Finally, events hooked on {@link btrplace.plan.Action.Hook#post}
     * are propagated
     *
     * @param a the event to propagate
     * @return {@code true} iff the action has been allowed by all the validators.
     */
    public boolean fireAction(Action a) {

        for (Event e : a.getEvents(Action.Hook.pre)) {
            if (Boolean.TRUE.equals(e.visit(validatorDispatcher))) {
                e.visit(notificationDispatcher);
            } else {
                return false;
            }
        }
        if (Boolean.TRUE.equals(a.visit(validatorDispatcher))) {
            a.visit(notificationDispatcher);
        } else {
            return false;
        }

        for (Event e : a.getEvents(Action.Hook.post)) {
            if (Boolean.TRUE.equals(e.visit(validatorDispatcher))) {
                e.visit(notificationDispatcher);
            } else {
                return false;
            }
        }
        return true;
    }

}
