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

/**
 * Store statistics about a solution.
 *
 * @author Fabien Hermenier
 */
public class SolutionStatistics {

    /**
     * The number of opened nodes at this point.
     */
    private long nbNodes;

    /**
     * The number of backtracks at this point.
     */
    private long nbBacktracks;

    /**
     * The time since the beginning of the solving process.
     */
    private long time;

    /**
     * The objective value if an objective was designed.
     */
    private int optValue;

    private boolean hasObjective = true;

    /**
     * Make a new statistics.
     *
     * @param nbN the number of opened nodes
     * @param nbB the number of backtracks
     * @param t   the time in milliseconds
     */
    public SolutionStatistics(long nbN, long nbB, long t) {
        this(nbN, nbB, t, -1);
        hasObjective = false;
    }

    /**
     * Make a new statistics.
     *
     * @param nbN the number of opened nodes
     * @param nbB the number of backtracks
     * @param t   the time in milliseconds
     * @param o   the value of the optimization variable
     */
    public SolutionStatistics(long nbN, long nbB, long t, int o) {
        nbNodes = nbN;
        nbBacktracks = nbB;
        time = t;
        optValue = o;
    }

    /**
     * Get the number of opened nodes.
     *
     * @return a positive number
     */
    public long getNbNodes() {
        return nbNodes;
    }

    /**
     * Get the number of backtracks.
     *
     * @return a positive number
     */
    public long getNbBacktracks() {
        return nbBacktracks;
    }

    /**
     * Get the time since the beginning of the solving process.
     *
     * @return a time in milliseconds
     */
    public long getTime() {
        return time;
    }

    /**
     * Get the value of the optimization variable.
     *
     * @return an integer
     */
    public int getOptValue() {
        return optValue;
    }

    /**
     * Indicates the presence of an optimization variable.
     *
     * @return {@code true} if there is an optimization variable
     */
    public boolean hasObjective() {
        return hasObjective;
    }
}
