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

package org.btrplace.scheduler.choco.runner.single;

import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.runner.SolutionStatistics;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Statistics related to a solving process on one instance.
 *
 * @author Fabien Hermenier
 */
public class SingleRunnerStatistics implements SolvingStatistics {

    private Parameters params;

    /**
     * The number of VMs actually managed by the problem.
     */
    private int nbManagedVMs;

    /**
     * The total duration of the solving process in milliseconds.
     */
    private long time;

    /**
     * The total number of opened nodes.
     */
    private long nbSearchNodes;

    /**
     * The total number of backtracks.
     */
    private long nbBacktracks;

    /**
     * Indicates whether or not the solver hits the timeout.
     */
    private boolean timeout;

    private int nbVMs;

    private int nbNodes;

    private List<SolutionStatistics> solutions;

    private int nbConstraints;

    private long coreRPBuildDuration;

    private long speRPDuration;

    private long start;

    /**
     * Make new statistics.
     *
     * @param n          the number of nodes in the model
     * @param v          the number of VMs in the model
     * @param c          the number of constraints
     * @param managedVMs the number of VMs managed by the algorithm.
     * @param st         the moment the computation starts (epoch format)
     * @param t          the solving duration in milliseconds
     * @param nbN        the number of opened nodes at the moment
     * @param nbB        the number of backtracks at the moment
     * @param to         {@code true} to indicate the solver hit a timeout
     * @param cd         the duration of the core-RP building process
     * @param sd         the duration of the core-RP specialization process
     */
    public SingleRunnerStatistics(Parameters ps, int n, int v, int c, int managedVMs, long st,
                                  long t, long nbN, long nbB, boolean to, long cd, long sd) {
        nbManagedVMs = managedVMs;
        this.params = ps;
        this.nbNodes = n;
        this.nbVMs = v;
        this.nbConstraints = c;
        time = t;
        this.start = st;
        nbSearchNodes = nbN;
        nbBacktracks = nbB;
        this.timeout = to;
        solutions = new ArrayList<>();
        this.coreRPBuildDuration = cd;
        this.speRPDuration = sd;
    }

    @Override
    public int getNbConstraints() {
        return nbConstraints;
    }

    @Override
    public long getSolvingDuration() {
        return time;
    }


    @Override
    public long getCoreRPBuildDuration() {
        return coreRPBuildDuration;
    }

    @Override
    public long getSpeRPDuration() {
        return speRPDuration;
    }

    @Override
    public long getNbSearchNodes() {
        return nbSearchNodes;
    }

    @Override
    public long getNbBacktracks() {
        return nbBacktracks;
    }

    @Override
    public boolean hitTimeout() {
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

    @Override
    public List<SolutionStatistics> getSolutions() {
        return solutions;
    }

    @Override
    public int getNbVMs() {
        return nbVMs;
    }

    @Override
    public int getNbNodes() {
        return nbNodes;
    }

    @Override
    public int getNbManagedVMs() {
        return nbManagedVMs;
    }

    @Override
    public long getStart() {
        return start;
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

        if (params.doOptimize()) {
            b.append("; optimize");
        }
        if (params.getTimeLimit() > 0) {
            b.append("; timeout: ").append(params.getTimeLimit()).append("s");
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
            b.append("\t").append(i).append(')')
                    .append(" at ").append(st.getTime()).append("ms: ")
                    .append(st.getNbNodes()).append(" node(s), ")
                    .append(st.getNbBacktracks()).append(" backtrack(s)");
            if (st.hasObjective()) {
                b.append(", objective: ").append(st.getOptValue());
            }
            b.append('\n');
            i++;
        }
        return b.toString();
    }

    @Override
    public Parameters getParameters() {
        return params;
    }
}
