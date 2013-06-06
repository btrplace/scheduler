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

import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.ReconfigurationAlgorithm;
import btrplace.solver.SolverException;
import btrplace.solver.choco.constraint.ConstraintMapper;
import btrplace.solver.choco.durationEvaluator.DurationEvaluators;
import btrplace.solver.choco.view.ModelViewMapper;

import java.util.Collection;

/**
 * A reconfiguration algorithm based on the Choco constraint solver.
 *
 * @author Fabien Hermenier
 */
public interface ChocoReconfigurationAlgorithm extends ReconfigurationAlgorithm, ChocoReconfigurationAlgorithmParams {

    /**
     * Indicate if the algorithm repair the model.
     *
     * @return {@code true} iff it repairs the model.
     */
    boolean doRepair();

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
     * Get the timeout value.
     *
     * @return a positive integer in seconds to indicate the timeout value or a negative value to
     *         indicate no timeout has been set
     */
    int getTimeLimit();

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
     * Get the maximum duration of a reconfiguration plan.
     *
     * @return a positive integer
     */
    int getMaxEnd();

    /**
     * Get the verbosity level of the solver.
     *
     * @return the verbosity level.
     * @see {@link #setVerbosity(int)} for more informations about the available levels
     */
    int getVerbosity();

    /**
     * Compute a reconfiguration plan to reach a solution to the model.
     * The {@link btrplace.model.constraint.MinMTTR} optimization constraint is used
     *
     * @param i     the current model
     * @param cstrs the satisfaction-oriented constraints that must be considered
     * @return the plan to execute to reach the new solution or {@code null} if there is no
     *         solution.
     * @throws SolverException if an error occurred while trying to solve the problem
     */
    ReconfigurationPlan solve(Model i, Collection<SatConstraint> cstrs) throws SolverException;
}
