/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan;

import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.Event;
import org.btrplace.plan.event.EventCommittedListener;
import org.btrplace.plan.event.NotificationDispatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * A skeleton for {@link ReconfigurationPlanApplier}.
 * It provides the material
 * to propagate the notifications related to the termination of actions and events.
 *
 * @author Fabien Hermenier
 */
public abstract class DefaultPlanApplier implements ReconfigurationPlanApplier {

  private final List<EventCommittedListener> listeners;

  private final NotificationDispatcher notificationDispatcher;

    /**
     * Make a new applier.
     */
    protected DefaultPlanApplier() {
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
     * Events hooked on {@link org.btrplace.plan.event.Action.Hook#PRE} are propagated in first
     * Then the real action is propagated. Finally, events hooked on {@link org.btrplace.plan.event.Action.Hook#POST}
     * are propagated
     *
     * @param a the event to propagate
     */
    public void fireAction(Action a) {

        for (Event e : a.getEvents(Action.Hook.PRE)) {
            e.visit(notificationDispatcher);
        }
        a.visit(notificationDispatcher);

        for (Event e : a.getEvents(Action.Hook.POST)) {
            e.visit(notificationDispatcher);
        }
    }

}
