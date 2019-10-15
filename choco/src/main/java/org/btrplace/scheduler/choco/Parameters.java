/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco;

import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.constraint.ChocoMapper;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.btrplace.scheduler.choco.transition.TransitionFactory;
import org.btrplace.scheduler.choco.view.ChocoView;
import org.chocosolver.solver.Settings;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Parameters for a {@link ChocoScheduler}.
 *
 * @author Fabien Hermenier
 */
public interface Parameters {

    /**
     * Set a seed to use from every random number generators.
     *
     * @param s the seed
     * @return {@code this}
     */
    Parameters setRandomSeed(long s);

    /**
     * Get the seed used by the random number generators.
     *
     * @return the seed
     */
    long getRandomSeed();

    /**
     * State if the algorithm only have to repair the model instead
     * of rebuilding a complete new solution.
     *
     * @param b {@code true} to repair
     * @return the current instance
     */
    Parameters doRepair(boolean b);

    /**
     * Indicate if the algorithm repairs the model.
     * Each constraint will scan the model to detect the minimum set of VMs
     * that should be managed to get able to get a solution.
     * This approach can reduce drastically the solving duration but might be too aggressive
     * then remove possible solutions.
     * @return {@code true} iff it repairs the model.
     */
    boolean doRepair();

    /**
     * State if the algorithm must try to improve the first computed solution.
     *
     * @param b {@code true} to make the algorithm try to improve the solution
     * @return the current instance
     */
    Parameters doOptimize(boolean b);

    /**
     * Tell is the solver tries to improve the first computed solution.
     *
     * @return {@code true} iff it try to improve the solution
     */
    boolean doOptimize();

    /**
     * Set the timeout value for the solving process.
     * Use a negative number to remove any timeout.
     *
     * @param t the timeout value, in second.
     * @return the current instance
     */
    Parameters setTimeLimit(int t);

    /**
     * Get the timeout value.
     *
     * @return a positive integer in seconds to indicate the timeout value or a negative value to
     * indicate no timeout has been set
     */
    int getTimeLimit();

    /**
     * Get the mapper that converts api-side elements to their choco implementation.
     *
     * @return the mapper.
     */
    ChocoMapper getMapper();

    /**
     * set the mapper that converts api-side elements to their choco implementation.
     *
     * @param map the mapper to use
     * @return the current instance
     */
    Parameters setMapper(ChocoMapper map);

    /**
     * Get the evaluator that is used to indicate the estimated duration of each action.
     *
     * @return the evaluator
     */
    DurationEvaluators getDurationEvaluators();

    /**
     * Set the duration evaluators to use.
     *
     * @param dev the evaluator to use
     * @return the current instance
     */
    Parameters setDurationEvaluators(DurationEvaluators dev);

    /**
     * Set the maximum duration of a reconfiguration plan.
     *
     * @param end a positive integer
     * @return the current instance
     */
    Parameters setMaxEnd(int end);

    /**
     * Get the maximum duration of a reconfiguration plan.
     *
     * @return a positive integer
     */
    int getMaxEnd();

    /**
     * Set the verbosity level of the solver.
     * <ul>
     * <li>Level 0: no information about the solving process</li>
     * <li>Level 1: variables are labelled</li>
     * <li>Level 2: solutions of Choco are printed</li>
     * <li>Level 3: choices are printed</li>
     * </ul>
     *
     * @param lvl the verbosity level
     * @return the current instance
     */
    Parameters setVerbosity(int lvl);

    /**
     * Get the verbosity level of the solver.
     * <ul>
     *     <li>at {@code 0}: no output</li>
     *     <li>at {@code 1}: variables are labeled to ease debugging. Every solutions are printed out</li>
     *     <li>at {@code 2}: statistics are printed out every second</li>
     *     <li>at {@code 3}: decisions are printed out</li>
     *     <li>at {@code 4}: contradictions are printed out</li>
     * </ul>
     * @return the verbosity level.
     * @see #setVerbosity(int)
     */
    int getVerbosity();

    /**
     * Set the factory that is used to model the transitions.
     *
     * @param amf the factory to rely on
     * @return {@code this}
     */
    Parameters setTransitionFactory(TransitionFactory amf);

    /**
     * Get the current factory that is used to model the transitions.
     *
     * @return the factory
     */
    TransitionFactory getTransitionFactory();

    /**
     * Get the environment factory.
     *
     * @return the registered factory.
     */
    EnvironmentFactory getEnvironmentFactory();

    /**
     * Set the environment factory to use to get the memory environment of Choco
     *
     * @param f the factory to use
     * @return {@code this}
     */
    Parameters setEnvironmentFactory(EnvironmentFactory f);

    /**
     * Declare a standalone view to be plugged inside the solver.
     * The class will be automatically instantiated at the beginning of the solver
     * specialisation phase. It must provided a default constructor
     *
     * @param v the class of the view to add
     * @return {@code true} if the view has been added
     */
    boolean addChocoView(Class<? extends ChocoView> v);

    /**
     * Remove a standalone view already plugged.
     *
     * @param v the view to remove
     * @return {@code true} if the view has been removed
     */
    boolean removeChocoView(Class<? extends ChocoView> v);

    /**
     * Get the solver settings.
     *
     * @return the solver settings.
     */
    Settings chocoSettings();

    /**
     * Set the choco settings.
     *
     * @param s the settings to set
     * @return {@code this}
     */
    Parameters chocoSettings(Settings s);

    /**
     * Get the standalone views.
     *
     * @return a list of views that may be empty
     */
    List<Class<? extends ChocoView>> getChocoViews();

    /**
     * Add a consumer to call every time a solution is computed.
     *
     * @param consumer the consumer to call
     * @return {@code this}
     */
    Parameters addSolutionListener(BiConsumer<ReconfigurationProblem, ReconfigurationPlan> consumer);

    /**
     * Returns the solution listeners.
     *
     * @return a list that may be empty.
     */
    List<BiConsumer<ReconfigurationProblem, ReconfigurationPlan>> solutionListeners();
}
