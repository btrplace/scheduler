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

package btrplace.solver.choco.runner;

import btrplace.solver.choco.ChocoReconfigurationAlgorithmParams;

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
    public int getSolvingDuration();

    /**
     * Get the time that was necessary to build the core-RP.
     *
     * @return a duration in milliseconds
     */
    public long getCoreRPBuildDuration();

    /**
     * Get the time that was necessary to specialize the core-CP.
     *
     * @return a duratio in milliseconds
     */
    public long getSpeRPDuration();

    /**
     * Get the number of opened nodes.
     *
     * @return a positive number
     */
    int getNbSearchNodes();

    /**
     * Get the number of backtracks.
     *
     * @return a positive number
     */
    int getNbBacktracks();

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
    public int getNbVMs();

    /**
     * Get the number of nodes in the model.
     *
     * @return a positive integer
     */
    public int getNbNodes();

    /**
     * Get the number of VMs managed by the algorithm.
     *
     * @return a positive number
     */
    public int getNbManagedVMs();

    /**
     * Get the parameters of the reconfiguration algorithm.
     *
     * @return a set of parameters
     */
    ChocoReconfigurationAlgorithmParams getParameters();
}
