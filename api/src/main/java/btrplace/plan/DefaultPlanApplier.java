/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.plan;

import btrplace.plan.event.Action;
import btrplace.plan.event.Event;
import btrplace.plan.event.EventCommittedListener;
import btrplace.plan.event.NotificationDispatcher;

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
     * Events hooked on {@link btrplace.plan.event.Action.Hook#pre} are propagated in first
     * Then the real action is propagated. Finally, events hooked on {@link btrplace.plan.event.Action.Hook#post}
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
