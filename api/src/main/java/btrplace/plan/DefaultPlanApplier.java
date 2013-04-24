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
        listeners = new ArrayList<>();
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
     * Events hooked on {@link btrplace.plan.Action.Hook#pre} are propagated.
     * Then the real action is propagated. Finally, events hooked on {@link btrplace.plan.Action.Hook#post}
     * are propagated
     *
     * @param a the event to propagate
     */
    public void fireAction(Action a) {
        for (Event e : a.getEvents(Action.Hook.pre)) {
            e.visit(this);
        }
        a.visit(this);
        for (Event e : a.getEvents(Action.Hook.post)) {
            e.visit(this);
        }

    }

    @Override
    public Object visit(SuspendVM a) {
        for (EventCommittedListener l : listeners) {
            l.committed(a);
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(Allocate a) {
        for (EventCommittedListener l : listeners) {
            l.committed(a);
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(AllocateEvent a) {
        for (EventCommittedListener l : listeners) {
            l.committed(a);
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(SubstitutedVMEvent a) {
        for (EventCommittedListener l : listeners) {
            l.committed(a);
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(BootNode a) {
        for (EventCommittedListener l : listeners) {
            l.committed(a);
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(BootVM a) {
        for (EventCommittedListener l : listeners) {
            l.committed(a);
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(ForgeVM a) {
        for (EventCommittedListener l : listeners) {
            l.committed(a);
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(KillVM a) {
        for (EventCommittedListener l : listeners) {
            l.committed(a);
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(MigrateVM a) {
        for (EventCommittedListener l : listeners) {
            l.committed(a);
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(ResumeVM a) {
        for (EventCommittedListener l : listeners) {
            l.committed(a);
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(ShutdownNode a) {
        for (EventCommittedListener l : listeners) {
            l.committed(a);
        }
        return Boolean.TRUE;
    }

    @Override
    public Object visit(ShutdownVM a) {
        for (EventCommittedListener l : listeners) {
            l.committed(a);
        }
        return Boolean.TRUE;
    }
}
