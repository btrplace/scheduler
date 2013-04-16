package btrplace.plan.event;

import btrplace.plan.EventCommittedListener;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class NotificationDispatcher implements ActionVisitor {

    private List<EventCommittedListener> listeners;

    public NotificationDispatcher(List<EventCommittedListener> l) {
        listeners = l;
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
