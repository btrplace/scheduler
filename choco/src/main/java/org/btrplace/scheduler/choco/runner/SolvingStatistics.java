/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco.runner;

import org.btrplace.model.Instance;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.Parameters;
import org.chocosolver.solver.search.measure.IMeasures;

import java.util.List;

/**
 * Simple interface to get statistics about a solving process.
 *
 * @author Fabien Hermenier
 */
public interface SolvingStatistics {

    /**
     * Get the solved instance.
     * @return the instance
     */
    Instance getInstance();

    /**
     * Get the time that was necessary to build the core-RP.
     *
     * @return a duration in milliseconds
     */
    long getCoreBuildDuration();

    /**
     * Get the time that was necessary to specialize the core-CP.
     *
     * @return a duration in milliseconds
     */
    long getSpecializationDuration();

    /**
     * Get the moment the computation starts.
     *
     * @return a time period in the epoch format
     */
    long getStart();

    /**
     * Get all the computed solutions ordered by time.
     *
     * @return a list of solutions that may be empty
     */
    List<SolutionStatistics> getSolutions();

    /**
     * Get the number of VMs managed by the algorithm.
     *
     * @return a positive number
     */
    int getNbManagedVMs();

    /**
     * Get the parameters of the scheduler.
     *
     * @return a set of parameters
     */
    Parameters getParameters();

    /**
     * Get the measures related to the solver.
     *
     * @return measures. {@code null} if the solver did not run
     */
    IMeasures getMeasures();

    /**
     * Check if the solver completed the search.
     *
     * @return {@code true} indicates the solver proved the optimality of the computed solution or that the problem is
     * not feasible (if no solution were computed)
     */
    boolean completed();

    /**
     * Get the last computed reconfiguration plan.
     *
     * @return a plan. {@code null} if there was no solution
     */
    ReconfigurationPlan lastSolution();

    /**
     * Summarizes as a CSV data.
     * Print the statistics as a CSV line.
     * Fields are separated by a ';' and ordered this way:
     * - getNbManagedVMs()
     * - getCoreBuildDuration()
     * - getSpecializationDuration()
     * - getMeasures().getTimeCount() * 1000 (so in milliseconds)
     * - solutions.size()
     * - completed ? 1 : 0
     *
     * @return a CSV formatted line.
     */
    String toCSV();
}
