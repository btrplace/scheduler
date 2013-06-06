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

import btrplace.solver.ReconfigurationAlgorithm;
import btrplace.solver.choco.constraint.SatConstraintMapper;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import btrplace.solver.choco.objective.ReconfigurationObjective;
import btrplace.solver.choco.view.ModelViewMapper;

/**
 * A basic configurable reconfiguration algorithm relying on Choco.
 *
 * @author Fabien Hermenier
 */
public interface ChocoSimpleReconfigurationAlgorithm extends ReconfigurationAlgorithm {

    /**
     * State if the algorithm only have to repair the model instead
     * of rebuilding a complete new solution.
     *
     * @param b {@code true} to repair
     */
    void doRepair(boolean b);

    /**
     * State if the algorithm must try to improve the first computed solution.
     *
     * @param b {@code true} to make the algorithm try to improve the solution
     */
    void doOptimize(boolean b);

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
     * Indicate if variables have to be labelled.
     * This is convenient for debugging but not activated by default.
     *
     * @param b {@code true} to create label for variables
     */
    void labelVariables(boolean b);

    /**
     * Set the objective to consider for this algorithm.
     *
     * @param o the objective
     */
    void setObjective(ReconfigurationObjective o);

    /**
     * Set the mapper that converts {@link btrplace.model.constraint.SatConstraint} to {@link btrplace.solver.choco.constraint.ChocoSatConstraint}.
     *
     * @param map the mapper to use
     */
    void setSatConstraintMapper(SatConstraintMapper map);

    /**
     * Set the duration evaluators to use.
     *
     * @param dev the evaluator to use
     */
    void setDurationEvaluators(DurationEvaluators dev);

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
     * Set the verbosity level of the solver.
     * At level 0, their is no information about the solving process.
     * Increasing the level increases the verbosity. The highest level
     * of verbosity is the level 3.
     *
     * @param lvl the level of verbosity
     */
    void setVerbosity(int lvl);
}
