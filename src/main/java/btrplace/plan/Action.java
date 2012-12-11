/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

import btrplace.model.Model;

import java.util.*;

/**
 * An action is an event that has to be scheduled for reliability purpose.
 * <p/>
 * An action may embed several other events that may be attached to a particular
 * hook.
 *
 * @author Fabien Hermenier
 */
public abstract class Action implements Event {

    /**
     * Possible moment where events can be considered.
     */
    public static enum Hook {
        /**
         * The event can be considered before executing the action.
         */
        pre,
        /**
         * The event can be considered after the action execution.
         */
        post
    }

    private int start;

    private int stop;

    private Map<Hook, List<Event>> events;

    /**
     * Create an action.
     *
     * @param st the moment the action starts
     * @param ed the moment the action ends
     */
    public Action(int st, int ed) {
        this.start = st;
        this.stop = ed;
        events = new HashMap<Hook, List<Event>>(2);
    }

    /**
     * Apply the action on a model.
     * In practice, the events with the {@link Hook#pre} are executed in first,
     * then {@link #applyAction(btrplace.model.Model)} is called. Finally,
     * the events with the {@link Hook#post} are executed.
     *
     * @param i the instance to alter with the action
     * @return {@code true} if the action and all the events were applied successfully
     */
    public boolean apply(Model i) {
        List<Event> nots = getEvents(Hook.pre);
        for (Event n : nots) {
            if (!n.apply(i)) {
                return false;
            }
        }
        if (!applyAction(i)) {
            return false;
        }
        nots = getEvents(Hook.post);
        for (Event n : nots) {
            if (!n.apply(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Apply only the scheduled action.
     *
     * @param i the model to modify
     * @return {@code true} if the action was applied successfully
     */
    public abstract boolean applyAction(Model i);

    /**
     * Get the moment the action starts.
     *
     * @return a positive integer
     */
    public int getStart() {
        return start;
    }

    /**
     * Get the moment the action ends.
     *
     * @return a positive integer
     */
    public int getEnd() {
        return stop;
    }


    /**
     * Add an event on the action.
     * The moment the event will be executed depends on its hook.
     *
     * @param k the hook
     * @param n the event to attach
     * @return
     */
    public boolean addEvent(Hook k, Event n) {
        List<Event> l = events.get(k);
        if (l == null) {
            l = new ArrayList<Event>();
            events.put(k, l);
        }
        return l.add(n);
    }

    /**
     * Get the events having a specific hook.
     *
     * @param k the hook
     * @return a list of events that may be empty
     */
    public List<Event> getEvents(Hook k) {
        List<Event> l = events.get(k);
        return l == null ? Collections.<Event>emptyList() : l;
    }

    /**
     * Pretty print of the action.
     *
     * @return a String
     */
    public abstract String pretty();

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("{action=").append(pretty());
        if (!events.isEmpty()) {
            for (Map.Entry<Hook, List<Event>> entry : events.entrySet()) {
                List<Event> l = entry.getValue();
                Hook k = entry.getKey();
                b.append(", @").append(k).append("= {");
                for (Iterator<Event> ite = l.iterator(); ite.hasNext(); ) {
                    b.append(ite.next());
                    if (ite.hasNext()) {
                        b.append(", ");
                    }
                }
                b.append("}");
            }
        }
        return b.toString();
    }
}
