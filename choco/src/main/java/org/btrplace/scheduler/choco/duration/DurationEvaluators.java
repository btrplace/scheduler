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

package org.btrplace.scheduler.choco.duration;

import org.btrplace.model.Element;
import org.btrplace.model.Model;
import org.btrplace.plan.event.*;
import org.btrplace.scheduler.SchedulerException;

import java.util.HashMap;
import java.util.Map;


/**
 * Class to store the {@link ActionDurationEvaluator} associated to each of the possible actions.
 * <p>
 * By default, each action is associated to a {@link ActionDurationFromOptionalAttribute} evaluator.
 * See https://github.com/fhermeni/btrplace-solver/wiki/attributes to get the attribute identifiers.
 * If the attribute is not set, a {@link ConstantActionDuration} is used and evaluate the duration to 1 second.
 *
 * @author Fabien Hermenier
 */
public class DurationEvaluators {

    private Map<Class<? extends Action>, ActionDurationEvaluator<Element>> durations;


    /**
     * Make a new {@code DurationEvaluators} and fulfill it
     * using default a default evaluator for each action.
     *
     * @return a fulfilled evaluators.
     */
    public static DurationEvaluators newBundle() {
        DurationEvaluators dev = new DurationEvaluators();

        //Default constructors
        dev.register(MigrateVM.class, new ActionDurationFromOptionalAttribute<>("migrate", new ConstantActionDuration<>(1)));
        dev.register(BootVM.class, new ActionDurationFromOptionalAttribute<>("boot", new ConstantActionDuration<>(1)));
        dev.register(ShutdownVM.class, new ActionDurationFromOptionalAttribute<>("shutdown", new ConstantActionDuration<>(1)));
        dev.register(SuspendVM.class, new ActionDurationFromOptionalAttribute<>("suspend", new ConstantActionDuration<>(1)));
        dev.register(ResumeVM.class, new ActionDurationFromOptionalAttribute<>("resume", new ConstantActionDuration<>(1)));
        dev.register(ForgeVM.class, new ActionDurationFromOptionalAttribute<>("forge", new ConstantActionDuration<>(1)));
        dev.register(ShutdownNode.class, new ActionDurationFromOptionalAttribute<>("shutdown", new ConstantActionDuration<>(1)));
        dev.register(BootNode.class, new ActionDurationFromOptionalAttribute<>("boot", new ConstantActionDuration<>(1)));
        dev.register(KillVM.class, new ActionDurationFromOptionalAttribute<>("kill", new ConstantActionDuration<>(1)));
        dev.register(Allocate.class, new ActionDurationFromOptionalAttribute<>("allocate", new ConstantActionDuration<>(1)));
        return dev;
    }

    /**
     * Make a new mapper.
     */
    public DurationEvaluators() {
        durations = new HashMap<>();
    }

    /**
     * Register a new {@link ActionDurationEvaluator}.
     *
     * @param a the action class
     * @param e the evaluator to register for the given action
     * @return {@code false} if this action delete a previous evaluator for that action
     */
    public boolean register(Class<? extends Action> a, ActionDurationEvaluator e) {
        return durations.put(a, e) == null;
    }

    /**
     * Un-register the {@link ActionDurationEvaluator} associated to a given
     * action, if exists.
     *
     * @param a the action class
     * @return {@code true} if a {@link ActionDurationEvaluator} was associated to the action.
     */
    public boolean unRegister(Class<? extends Action> a) {
        return durations.remove(a) != null;
    }

    /**
     * Check if a {@link ActionDurationEvaluator} is registered for a given action.
     *
     * @param a the action' class
     * @return {@code true} iff a {@link ActionDurationEvaluator} is registered for that action
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
    public ActionDurationEvaluator getEvaluator(Class<? extends Action> a) {
        return durations.get(a);
    }

    /**
     * Evaluate the duration of given action on a given element.
     *
     * @param mo the model to consider
     * @param a  the action' class
     * @param e  the element identifier
     * @return a positive number if the evaluation succeeded. A negative number otherwise
     */
    public int evaluate(Model mo, Class<? extends Action> a, Element e) throws SchedulerException {
        ActionDurationEvaluator<Element> ev = durations.get(a);
        if (ev == null) {
            throw new SchedulerException(null, "Unable to estimate the action duration related to '" + e + "'");
        }
        int d = ev.evaluate(mo, e);
        if (d <= 0) {
            throw new SchedulerException(null, "Unable to estimate the action duration related to '" + e + "'");
        }
        return d;
    }
}
