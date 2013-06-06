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

package btrplace.solver.choco;

import btrplace.solver.choco.constraint.ConstraintMapper;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import btrplace.solver.choco.view.ModelViewMapper;

/**
 * Parameters for a {@link ChocoReconfigurationAlgorithm}.
 *
 * @author Fabien Hermenier
 */
public interface ChocoReconfigurationAlgorithmParams {

    /**
     * State if the algorithm only have to repair the model instead
     * of rebuilding a complete new solution.
     *
     * @param b {@code true} to repair
     * @return the current instance
     */
    ChocoReconfigurationAlgorithmParams doRepair(boolean b);

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
    ChocoReconfigurationAlgorithmParams doOptimize(boolean b);

    /**
     * Tell is the solver tries to improve the first computed solution.
     *
     * @return {@code true} iff it try to improve the solution
     */
    boolean doOptimize();

    /**
     * Get the mapper that is used to associate the {@link btrplace.model.view.ModelView}
     * to the {@link btrplace.solver.choco.view.ChocoModelView}.
     *
     * @return the mapper
     */
    ModelViewMapper getViewMapper();

    /**
     * Set the mapper to use to associate the {@link btrplace.model.view.ModelView}
     * to the {@link btrplace.solver.choco.view.ChocoModelView}.
     *
     * @param m the mapper to use
     * @return the current instance
     */
    ChocoReconfigurationAlgorithmParams setViewMapper(ModelViewMapper m);

    /**
     * Set the timeout value for the solving process.
     * Use a negative number to remove any timeout.
     *
     * @param t the timeout value, in second.
     * @return the current instance
     */
    ChocoReconfigurationAlgorithmParams setTimeLimit(int t);

    /**
     * Get the timeout value.
     *
     * @return a positive integer in seconds to indicate the timeout value or a negative value to
     *         indicate no timeout has been set
     */
    int getTimeLimit();

    /**
     * Indicate if variables have to be labelled.
     * This is convenient for debugging but not activated by default.
     *
     * @param b {@code true} to create label for variables
     * @return the current instance
     */
    ChocoReconfigurationAlgorithmParams labelVariables(boolean b);

    /**
     * Indicate if variables are labelled.
     *
     * @return {@code true} iff the variables are labelled
     */
    boolean areVariablesLabelled();

    /**
     * Get the mapper that converts {@link btrplace.model.constraint.Constraint} to {@link btrplace.solver.choco.constraint.ChocoConstraint}.
     *
     * @return the mapper.
     */
    ConstraintMapper getConstraintMapper();

    /**
     * Set the mapper that converts {@link btrplace.model.constraint.Constraint} to {@link btrplace.solver.choco.constraint.ChocoConstraint}.
     *
     * @param map the mapper to use
     * @return the current instance
     */
    ChocoReconfigurationAlgorithmParams setConstraintMapper(ConstraintMapper map);

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
    ChocoReconfigurationAlgorithmParams setDurationEvaluators(DurationEvaluators dev);

    /**
     * Set the maximum duration of a reconfiguration plan.
     *
     * @param end a positive integer
     * @return the current instance
     */
    ChocoReconfigurationAlgorithmParams setMaxEnd(int end);

    /**
     * Get the maximum duration of a reconfiguration plan.
     *
     * @return a positive integer
     */
    int getMaxEnd();

    /**
     * Set the verbosity level of the solver.
     * At level 0, their is no information about the solving process.
     * Increasing the level increases the verbosity. The highest level
     * of verbosity is the level 3.
     *
     * @param lvl the level of verbosity
     * @return the current instance
     */
    ChocoReconfigurationAlgorithmParams setVerbosity(int lvl);

    /**
     * Get the verbosity level of the solver.
     *
     * @return the verbosity level.
     * @see {@link #setVerbosity(int)} for more informations about the available levels
     */
    int getVerbosity();
}
