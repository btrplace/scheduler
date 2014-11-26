/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.plan.event;

import java.util.List;

/**
 * Dispatcher that propagate every visited action or event
 * to a given list of {@link EventCommittedListener}.
 *
 * @author Fabien Hermenier
 */
public class NotificationDispatcher implements ActionVisitor {

    private List<EventCommittedListener> listeners;

    /**
     * Make a new dispatcher.
     *
     * @param l the listener to notify for each of the visited actions and event.
     */
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
