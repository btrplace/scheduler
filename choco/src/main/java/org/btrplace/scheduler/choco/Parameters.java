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

package org.btrplace.scheduler.choco;

import org.btrplace.scheduler.choco.constraint.ConstraintMapper;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.btrplace.scheduler.choco.transition.TransitionFactory;
import org.btrplace.scheduler.choco.view.ModelViewMapper;
import org.btrplace.scheduler.choco.view.SolverViewBuilder;

import java.util.Collection;

/**
 * Parameters for a {@link ChocoScheduler}.
 *
 * @author Fabien Hermenier
 */
public interface Parameters {

    /**
     * State if the algorithm only have to repair the model instead
     * of rebuilding a complete new solution.
     *
     * @param b {@code true} to repair
     * @return the current instance
     */
    Parameters doRepair(boolean b);

    /**
     * Indicate if the algorithm repair the model.
     *
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
     * Get the mapper that is used to associate the {@link org.btrplace.model.view.ModelView}
     * to the {@link org.btrplace.scheduler.choco.view.ChocoView}.
     *
     * @return the mapper
     */
    ModelViewMapper getViewMapper();

    /**
     * Set the mapper to use to associate the {@link org.btrplace.model.view.ModelView}
     * to the {@link org.btrplace.scheduler.choco.view.ChocoView}.
     *
     * @param m the mapper to use
     * @return the current instance
     */
    Parameters setViewMapper(ModelViewMapper m);

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
     * Get the mapper that converts {@link org.btrplace.model.constraint.Constraint} to {@link org.btrplace.scheduler.choco.constraint.ChocoConstraint}.
     *
     * @return the mapper.
     */
    ConstraintMapper getConstraintMapper();

    /**
     * Set the mapper that converts {@link org.btrplace.model.constraint.Constraint} to {@link org.btrplace.scheduler.choco.constraint.ChocoConstraint}.
     *
     * @param map the mapper to use
     * @return the current instance
     */
    Parameters setConstraintMapper(ConstraintMapper map);

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
     *
     * @return the verbosity level.
     * @see #setVerbosity(int)
     */
    int getVerbosity();

    /**
     * Set the factory that is used to model the transitions.
     *
     * @param amf the factory to rely on
     */
    void setTransitionFactory(TransitionFactory amf);

    /**
     * Get the current factory that is used to model the transitions.
     *
     * @return the factory
     */
    TransitionFactory getTransitionFactory();

    /**
     * Declare a builder that create solve-only views.
     *
     * @param b the builder to add
     */
    void addSolverViewBuilder(SolverViewBuilder b);

    /**
     * Remove a builder dedicated to solver-only views.
     *
     * @param b the builder to remove
     * @return {@code true} iff the builder has been removed
     */
    boolean removeSolverViewBuilder(SolverViewBuilder b);

    /**
     * Get the solver-only view builders.
     *
     * @return a collection that may be empty
     */
    Collection<SolverViewBuilder> getSolverViews();
}
