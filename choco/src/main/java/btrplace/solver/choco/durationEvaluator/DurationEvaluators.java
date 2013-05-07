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

package btrplace.solver.choco.durationEvaluator;

import btrplace.plan.event.Action;
import btrplace.solver.SolverException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class to store the {@link DurationEvaluator} associated to each of the possible actions.
 *
 * @author Fabien Hermenier
 */
public class DurationEvaluators {

    private Map<Class<? extends Action>, DurationEvaluator> durations;

    /**
     * Make a new mapper.
     */
    public DurationEvaluators() {
        durations = new HashMap<>();


        //Default constructors
        durations.put(btrplace.plan.event.MigrateVM.class, new ConstantDuration(1));
        durations.put(btrplace.plan.event.BootVM.class, new ConstantDuration(1));
        durations.put(btrplace.plan.event.ShutdownVM.class, new ConstantDuration(1));
        durations.put(btrplace.plan.event.SuspendVM.class, new ConstantDuration(1));
        durations.put(btrplace.plan.event.ResumeVM.class, new ConstantDuration(1));
        durations.put(btrplace.plan.event.ForgeVM.class, new ConstantDuration(1));
        durations.put(btrplace.plan.event.ShutdownNode.class, new ConstantDuration(1));
        durations.put(btrplace.plan.event.BootNode.class, new ConstantDuration(1));
        durations.put(btrplace.plan.event.KillVM.class, new ConstantDuration(1));
    }

    /**
     * Register a new {@link DurationEvaluator}.
     *
     * @param a the action class
     * @param e the evaluator to register for the given action
     * @return {@code false} if this action delete a previous evaluator for that action
     */
    public boolean register(Class<? extends Action> a, DurationEvaluator e) {
        return durations.put(a, e) == null;
    }

    /**
     * Un-register the {@link DurationEvaluator} associated to a given
     * action, if exists.
     *
     * @param a the action class
     * @return {@code true} if a {@link DurationEvaluator} was associated to the action.
     */
    public boolean unregister(Class<? extends Action> a) {
        return durations.remove(a) != null;
    }

    /**
     * Check if a {@link DurationEvaluator} is registered for a given action.
     *
     * @param a the action' class
     * @return {@code true} iff a {@link DurationEvaluator} is registered for that action
     */
    public boolean isRegistered(Class<? extends Action> a) {
        return durations.containsKey(a);
    }

    /**
     * Get the evaluator associated to a given action.
     *
     * @param a the action' class
     * @return the registered evaluator, if exists
     */
    public DurationEvaluator getEvaluator(Class<? extends Action> a) {
        return durations.get(a);
    }

    /**
     * Evaluate the duration of given action on a given element.
     *
     * @param a the action' class
     * @param e the element
     * @return a positive number if the evaluation succeeded. A negative number otherwise
     */
    public int evaluate(Class<? extends Action> a, UUID e) throws SolverException {
        DurationEvaluator ev = durations.get(a);
        if (ev == null) {
            throw new SolverException(null, "Unable to estimate the action duration related to '" + e + "'");
        }
        int d = ev.evaluate(e);
        if (d < 0) {
            throw new SolverException(null, "Unable to estimate the action duration related to '" + e + "'");
        }
        return d;
    }
}
