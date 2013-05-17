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

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Statistics related to a solving process.
 *
 * @author Fabien Hermenier
 */
public class SolvingStatistics {

    /**
     * The number of VMs actually managed by the problem.
     */
    private int nbManagedVMs;

    /**
     * The total duration of the solving process in milliseconds.
     */
    private int time;

    /**
     * The total number of opened nodes.
     */
    private int nbSearchNodes;

    /**
     * The total number of backtracks.
     */
    private int nbBacktracks;

    /**
     * Indicates whether or not the solver hits the timeout.
     */
    private boolean timeout;

    private int nbVMs;

    private int nbNodes;

    private boolean doOptimize;

    private int maxDuration;

    private Set<SolutionStatistics> solutions;

    private int nbConstraints;

    private long coreRPBuildDuration;

    private long speRPDuration;

    /**
     * Compare the solution by their moment. If equal, the number of nodes then the number of backtracks.
     */
    private static Comparator<SolutionStatistics> solutionsCmp = new Comparator<SolutionStatistics>() {
        @Override
        public int compare(SolutionStatistics sol1, SolutionStatistics sol2) {
            if (sol1.getTime() == sol2.getTime()) {
                //Compare wrt. the number of nodes or backtracks
                if (sol1.getNbNodes() == sol2.getNbNodes()) {
                    return sol1.getNbBacktracks() - sol2.getNbBacktracks();
                }
                return sol1.getNbNodes() - sol2.getNbNodes();
            }
            return sol1.getTime() - sol2.getTime();
        }
    };

    /**
     * Make new statistics.
     *
     * @param nbNodes             the number of nodes in the model
     * @param nbVMs               the number of VMs in the model
     * @param nbConstraints       the number of constraints
     * @param doOptimize          {@code true} to indicate the solver tried to improve the computed solution
     * @param timeout             the timeout value for the solver in seconds
     * @param managedVMs          the number of VMs managed by the algorithm.
     * @param t                   the solving duration in milliseconds
     * @param nbN                 the number of opened nodes at the moment
     * @param nbB                 the number of backtracks at the moment
     * @param to                  {@code true} to indicate the solver hit a timeout
     * @param coreRPBuildDuration the duration of the core-RP building process
     * @param speRPDuration       the duration of the core-RP specialization process
     */
    public SolvingStatistics(int nbNodes, int nbVMs, int nbConstraints, boolean doOptimize, int timeout, int managedVMs,
                             int t, int nbN, int nbB, boolean to, long coreRPBuildDuration, long speRPDuration) {
        nbManagedVMs = managedVMs;
        this.nbNodes = nbNodes;
        this.nbVMs = nbVMs;
        this.nbConstraints = nbConstraints;
        time = t;
        nbSearchNodes = nbN;
        nbBacktracks = nbB;
        this.maxDuration = timeout;
        this.timeout = to;
        this.doOptimize = doOptimize;
        solutions = new TreeSet<>(solutionsCmp);
        this.coreRPBuildDuration = coreRPBuildDuration;
        this.speRPDuration = speRPDuration;
    }

    /**
     * Get the number of constraints to satisfy
     *
     * @return a positive number
     */
    public int getNbConstraints() {
        return nbConstraints;
    }

    /**
     * Get the time since the beginning of the solving process.
     *
     * @return a duration in milliseconds
     */
    public int getSolvingDuration() {
        return time;
    }

    /**
     * Get the time that was necessary to build the core-RP.
     *
     * @return a duration in milliseconds
     */
    public long getCoreRPBuildDuration() {
        return coreRPBuildDuration;
    }

    /**
     * Get the time that was necessary to specialize the core-CP.
     *
     * @return a duratio in milliseconds
     */
    public long getSpeRPDuration() {
        return speRPDuration;
    }

    /**
     * Get the number of opened nodes.
     *
     * @return a positive number
     */
    public int getNbSearchNodes() {
        return nbSearchNodes;
    }

    /**
     * Get the number of backtracks.
     *
     * @return a positive number
     */
    public int getNbBacktracks() {
        return nbBacktracks;
    }

    /**
     * Indicates if the solver hit a timeout.
     *
     * @return {@code true} iff the solver hit a timeout
     */
    public boolean isTimeout() {
        return timeout;
    }

    /**
     * Add a solution to the statistics.
     *
     * @param so the solution to add
     */
    public void addSolution(SolutionStatistics so) {
        this.solutions.add(so);
    }

    /**
     * Get all the computed solutions ordered by time.
     *
     * @return a list of solutions that may be empty
     */
    public Set<SolutionStatistics> getSolutions() {
        return solutions;
    }

    /**
     * Get the number of VMs in the model.
     *
     * @return a positive integer
     */
    public int getNbVMs() {
        return nbVMs;
    }

    /**
     * Get the number of nodes in the model.
     *
     * @return a positive integer
     */
    public int getNbNodes() {
        return nbNodes;
    }

    /**
     * Tell if the solver tried to optimize the computed solution.
     *
     * @return {@code true} iff the solver was configured for optimization
     */
    public boolean doOptimize() {
        return doOptimize;
    }

    /**
     * Get the maximum solving duration.
     *
     * @return a duration is seconds. A negative number for no timeout.
     */
    public int getTimeout() {
        return maxDuration;
    }

    /**
     * Get the number of VMs managed by the algorithm.
     *
     * @return a positive number
     */
    public int getNbManagedVMs() {
        return nbManagedVMs;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(nbNodes).append(" node(s)")
                .append("; ").append(nbVMs).append(" VM(s)");
        if (nbManagedVMs != nbVMs) {
            b.append(" (").append(nbManagedVMs).append(" managed)");
        }
        b.append("; ").append(nbConstraints).append(" constraint(s)");

        if (doOptimize) {
            b.append("; optimize");
        }
        if (maxDuration > 0) {
            b.append("; timeout: ").append(maxDuration).append("s");
        }
        b.append("\nBuilding duration: ").append(coreRPBuildDuration).append("ms (core-RP) + ").append(speRPDuration).append("ms (specialization)");
        b.append("\nAfter ").append(time).append("ms of search");
        if (timeout) {
            b.append(" (timeout)");
        } else {
            b.append(" (terminated)");
        }
        b.append(": ")
                .append(nbSearchNodes).append(" opened search node(s), ")
                .append(nbBacktracks).append(" backtrack(s), ")
                .append(solutions.size()).append(" solution(s)");
        if (!solutions.isEmpty()) {
            b.append(":\n");
        } else {
            b.append('.');
        }
        int i = 1;
        for (SolutionStatistics st : solutions) {
            b.append("\t").append(i).append(")")
                    .append(" at ").append(st.getTime()).append("ms: ")
                    .append(st.getNbNodes()).append(" node(s), ")
                    .append(st.getNbBacktracks()).append(" backtrack(s)");
            if (st.hasObjective()) {
                b.append(", objective: ").append(st.getOptValue());
            }
            b.append("\n");
            i++;
        }
        return b.toString();
    }
}
