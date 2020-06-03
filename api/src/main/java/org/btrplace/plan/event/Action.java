/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.plan.event;

import org.btrplace.model.Model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An action is an event that has to be scheduled for reliability purpose.
 * <p>
 * An action may embed several other events. Events are attach to the actions
 * using {@link Hook}.
 * hook.
 *
 * @author Fabien Hermenier
 */
public abstract class Action implements Event {

    /**
     * Possible moments where an event can be considered.
     */
    public enum Hook {
        /**
         * The event can be considered before executing the action.
         */
        PRE("pre"),
        /**
         * The event can be considered after the action execution.
         */
        POST("post");

      private final String name;

        /**
         * new hook
         *
         * @param v the pretty name
         */
        Hook(String v) {
            this.name = v;
        }


        @Override
        public String toString() {
            return this.name;
        }
    }

  private final int start;

  private final int stop;

  private final EnumMap<Hook, Set<Event>> events;

    /**
     * Create an action.
     *
     * @param st the moment the action starts
     * @param ed the moment the action ends
     */
    public Action(int st, int ed) {
        this.start = st;
        this.stop = ed;
        events = new EnumMap<>(Hook.class);
    }

    /**
     * Apply the action on a model.
     * In practice, the events attached to {@link Hook#PRE} are executed in first,
     * then {@link #applyAction(org.btrplace.model.Model)} is called. Finally,
     * the events attached to {@link Hook#POST} are executed.
     *
     * @param i the instance to alter with the action
     * @return {@code true} if the action and all the events were applied successfully
     */
    @Override
    public boolean apply(Model i) {
        return applyEvents(Hook.PRE, i) && applyAction(i) && applyEvents(Hook.POST, i);
    }

    /**
     * Apply the events attached to a given hook.
     *
     * @param k the hook
     * @param i the model to modify with the application of the events
     * @return {@code true} iff all the events were applied successfully
     */
    public boolean applyEvents(Hook k, Model i) {
        for (Event n : getEvents(k)) {
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
     * Add an event to the action.
     * The moment the event will be executed depends on its hook.
     *
     * @param k the hook
     * @param n the event to attach
     * @return {@code true} iff the event was added
     */
    public boolean addEvent(Hook k, Event n) {
      events.putIfAbsent(k, new HashSet<>());
      return events.get(k).add(n);
    }

    /**
     * Get the events attached to a specific hook.
     *
     * @param k the hook
     * @return a list of events that may be empty
     */
    public Set<Event> getEvents(Hook k) {
      return events.getOrDefault(k, Collections.emptySet());
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
            for (Map.Entry<Hook, Set<Event>> entry : events.entrySet()) {
                Set<Event> l = entry.getValue();
                Hook k = entry.getKey();
                b.append(", @").append(k).append("= {");
                b.append(l.stream().map(Object::toString).collect(Collectors.joining(", ")));
                b.append('}');
            }
        }
        b.append('}');
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Action action = (Action) o;

        return start == action.start && stop == action.stop && events.equals(action.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, stop, events);
    }
}
