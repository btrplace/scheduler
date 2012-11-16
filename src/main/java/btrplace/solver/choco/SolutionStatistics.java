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

/**
 * Store statistics about a solution.
 *
 * @author Fabien Hermenier
 */
public class SolutionStatistics {

    /**
     * The number of opened nodes at this point.
     */
    private int nbNodes;

    /**
     * The number of backtracks at this point.
     */
    private int nbBacktracks;

    /**
     * The moment the solver computed the solution.
     */
    private int time;

    /**
     * The objective value if an objective was designed.
     */
    private int optValue;

    public SolutionStatistics(int nbN, int nbB, int t) {
        this(nbN, nbB, t, -1);
    }

    public SolutionStatistics(int nbN, int nbB, int t, int o) {
        nbNodes = nbN;
        nbBacktracks = nbB;
        time = t;
        optValue = o;
    }

    public int getNbNodes() {
        return nbNodes;
    }

    public int getNbBacktracks() {
        return nbBacktracks;
    }

    public int getTime() {
        return time;
    }

    public int getOptValue() {
        return optValue;
    }
}
