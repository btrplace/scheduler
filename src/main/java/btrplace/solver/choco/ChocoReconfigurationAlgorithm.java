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

import btrplace.plan.ReconfigurationAlgorithm;

/**
 * A reconfiguration algorithm based on the Choco constraint solver.
 *
 * @author Fabien Hermenier
 */
public interface ChocoReconfigurationAlgorithm extends ReconfigurationAlgorithm {


    void doOptimize(boolean b);

    boolean doOptimize();

    /**
     * Set the timeout value in seconds.
     * Use a negative number to remove any timeout.
     *
     * @param t
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
     * Get the mapper that convert {@link btrplace.model.SatConstraint} to {@link ChocoSatConstraint}.
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
}
