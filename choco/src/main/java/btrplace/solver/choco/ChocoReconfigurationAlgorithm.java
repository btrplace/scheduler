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

package btrplace.solver.choco;

import btrplace.solver.ReconfigurationAlgorithm;
import btrplace.solver.choco.constraint.SatConstraintMapper;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import btrplace.solver.choco.objective.ReconfigurationObjective;
import btrplace.solver.choco.view.ModelViewMapper;

/**
 * A reconfiguration algorithm based on the Choco constraint solver.
 *
 * @author Fabien Hermenier
 */
public interface ChocoReconfigurationAlgorithm extends ReconfigurationAlgorithm {

    /**
     * State if the algorithm only have to repair the model instead
     * of rebuilding a complete new solution.
     *
     * @param b {@code true} to repair
     */
    void repair(boolean b);

    /**
     * Indicate if the algorithm repair the model.
     *
     * @return {@code true} iff it repairs the model.
     */
    boolean repair();

    /**
     * State if the algorithm must try to improve the first computed solution.
     *
     * @param b {@code true} to make the algorithm try to improve the solution
     */
    void doOptimize(boolean b);

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
     */
    void setViewMapper(ModelViewMapper m);

    /**
     * Set the timeout value for the solving process.
     * Use a negative number to remove any timeout.
     *
     * @param t the timeout value, in second.
     */
    void setTimeLimit(int t);

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
     */
    void labelVariables(boolean b);

    /**
     * Indicate if variables are labelled.
     *
     * @return {@code true} iff the variables are labelled
     */
    boolean areVariablesLabelled();

    /**
     * Get the objective associated to this algorithm.
     *
     * @return the stated objective.
     */
    ReconfigurationObjective getObjective();

    /**
     * Set the objective to consider for this algorithm.
     *
     * @param o the objective
     */
    void setObjective(ReconfigurationObjective o);

    /**
     * Get the mapper that convert {@link btrplace.model.constraint.SatConstraint} to {@link btrplace.solver.choco.constraint.ChocoSatConstraint}.
     *
     * @return the mapper.
     */
    SatConstraintMapper getSatConstraintMapper();

    /**
     * Get the evaluator that is used to indicate the estimated duration of each action.
     *
     * @return the evaluator
     */
    DurationEvaluators getDurationEvaluators();

    /**
     * Get statistics about the solving process
     *
     * @return some statistics
     */
    SolvingStatistics getSolvingStatistics();

    /**
     * Set the maximum duration of a reconfiguration plan.
     *
     * @param end a positive integer
     */
    void setMaxEnd(int end);

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
     */
    void setVerbosity(int lvl);
}
