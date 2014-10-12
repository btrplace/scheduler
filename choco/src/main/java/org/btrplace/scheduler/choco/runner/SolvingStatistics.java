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

package org.btrplace.scheduler.choco.runner;

import org.btrplace.scheduler.choco.Parameters;

import java.util.List;

/**
 * Simple interface to get statistics about a solving process.
 *
 * @author Fabien Hermenier
 */
public interface SolvingStatistics {

    /**
     * Get the number of constraints to satisfy
     *
     * @return a positive number
     */
    int getNbConstraints();

    /**
     * Get the time since the beginning of the solving process.
     *
     * @return a duration in milliseconds
     */
    long getSolvingDuration();

    /**
     * Get the time that was necessary to build the core-RP.
     *
     * @return a duration in milliseconds
     */
    long getCoreRPBuildDuration();

    /**
     * Get the time that was necessary to specialize the core-CP.
     *
     * @return a duration in milliseconds
     */
    long getSpeRPDuration();

    /**
     * Get the moment the computation starts.
     *
     * @return a time period in the epoch format
     */
    long getStart();

    /**
     * Get the number of opened nodes.
     *
     * @return a positive number
     */
    long getNbSearchNodes();

    /**
     * Get the number of backtracks.
     *
     * @return a positive number
     */
    long getNbBacktracks();

    /**
     * Indicates if the solver hit a timeout.
     *
     * @return {@code true} iff the solver hit a timeout
     */
    boolean hitTimeout();

    /**
     * Get all the computed solutions ordered by time.
     *
     * @return a list of solutions that may be empty
     */
    List<SolutionStatistics> getSolutions();

    /**
     * Get the number of VMs in the model.
     *
     * @return a positive integer
     */
    int getNbVMs();

    /**
     * Get the number of nodes in the model.
     *
     * @return a positive integer
     */
    int getNbNodes();

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
}
