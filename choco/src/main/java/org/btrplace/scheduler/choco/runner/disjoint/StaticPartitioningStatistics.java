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

package org.btrplace.scheduler.choco.runner.disjoint;

import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.runner.SolutionStatistics;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Statistics for a solving process partitioned statically.
 *
 * @author Fabien Hermenier
 */
public class StaticPartitioningStatistics implements SolvingStatistics {

    private List<SolvingStatistics> partResults;

    private int nbNodes, nbVMs, nbConstraints, nbManaged, coreRPDuration, speRPDuration;

    private int nbWorkers, nbSearchNodes, nbBacktracks, nbPartitions;

    private long splitDuration, duration, start;

    private boolean hitTimeout;

    private Parameters params;

    /**
     * Make the statistics.
     *
     * @param ps      the standard parameters for the solving process
     * @param n       the number of nodes in the model
     * @param v       the number of VMs in the model
     * @param c       the number of satisfaction-oriented constraints.
     * @param st      the moment the computation started, epoch format
     * @param sd      the duration of the splitting process in milliseconds
     * @param d       the solving process duration in milliseconds
     * @param w       the number of workers to solve the partitions in parallel
     * @param nbParts the number of partitions to compute
     */
    public StaticPartitioningStatistics(Parameters ps, int n, int v, int c,
                                        long st, long sd, long d, int w, int nbParts) {
        partResults = new ArrayList<>();
        this.start = st;
        this.nbNodes = n;
        this.nbVMs = v;
        this.nbConstraints = c;
        this.duration = d;
        nbSearchNodes = 0;
        nbBacktracks = 0;
        nbManaged = 0;
        coreRPDuration = 0;
        speRPDuration = 0;
        hitTimeout = false;
        this.nbWorkers = w;
        params = ps;
        this.splitDuration = sd;
        this.nbPartitions = nbParts;
    }

    @Override
    public long getSolvingDuration() {
        return duration;
    }

    @Override
    public long getCoreRPBuildDuration() {
        return coreRPDuration;
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
        return hitTimeout;
    }

    /**
     * Get the computed solutions.
     * To get a solution, it is expected to have one solution for each partition.
     * This method does not return all the reported solutions. If some partitions get multiple solution,
     * only the last is considered.
     *
     * @return a list of solutions that may be empty
     */
    @Override
    public List<SolutionStatistics> getSolutions() {

        //Check for the first solution that concatenate all the first solutions.
        List<SolutionStatistics> solutions = new ArrayList<>();

        int firstN = 0, firstB = 0, firstOptValue = 0;
        int lastN = 0, lastB = 0, lastOptValue = 0;

        //firstTime  == end of the last first solution
        //lastTime ==  end of the last computed partition solution
        long endFirst = start;
        long endLast = start;
        boolean multipleSolution = false;
        if (partResults.isEmpty()) {
            return solutions;
        }
        for (SolvingStatistics st : partResults) {
            if (st.getSolutions().isEmpty()) {
                //At least 1 partition does not have a result, so the problem is not totally solved
                return solutions;
            } else {
                SolutionStatistics first = st.getSolutions().get(0);
                firstN += first.getNbNodes();
                firstB += first.getNbBacktracks();
                firstOptValue += first.getOptValue();
                endFirst = Math.max(endFirst, st.getStart() + first.getTime());
                if (st.getSolutions().size() > 1) {
                    multipleSolution = true;
                    SolutionStatistics last = st.getSolutions().get(st.getSolutions().size() - 1);
                    lastN += last.getNbNodes();
                    lastB += last.getNbBacktracks();
                    lastOptValue += last.getOptValue();
                    endLast = Math.max(endLast, st.getStart() + last.getTime());
                } else {
                    lastN += first.getNbNodes();
                    lastB += first.getNbBacktracks();
                    lastOptValue += first.getOptValue();
                    endLast = Math.max(endLast, st.getStart() + first.getTime());
                }
            }
        }

        solutions.add(new SolutionStatistics(firstN, firstB, endFirst - start, firstOptValue));
        if (multipleSolution) {
            solutions.add(new SolutionStatistics(lastN, lastB, endLast - start, lastOptValue));
        }
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
        return nbManaged;
    }

    @Override
    public Parameters getParameters() {
        return params;
    }

    @Override
    public int getNbConstraints() {
        return nbConstraints;
    }

    /**
     * Get the number of partitions.
     *
     * @return a number >= 1
     */
    public int getNbParts() {
        return nbPartitions;
    }

    /**
     * Get the maximum number of workers to that works in parallel
     *
     * @return a number >= 1
     */
    public int getNbWorkers() {
        return nbWorkers;
    }

    /**
     * Add the statistics related to a partition.
     *
     * @param stats the partition statistics.
     */
    public void addPartitionStatistics(SolvingStatistics stats) {
        nbBacktracks += stats.getNbBacktracks();
        nbSearchNodes += stats.getNbSearchNodes();
        nbManaged += stats.getNbManagedVMs();
        hitTimeout |= stats.hitTimeout();
        coreRPDuration = (int) Math.max(coreRPDuration, stats.getCoreRPBuildDuration());
        speRPDuration = (int) Math.max(speRPDuration, stats.getSpeRPDuration());
        partResults.add(stats);
    }

    /**
     * Get the partition splitting duration in milliseconds.
     *
     * @return a positive value.
     */
    public long getSplitDuration() {
        return splitDuration;
    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        List<SolutionStatistics> stats = getSolutions();

        buildHeader(b, stats);

        if (!stats.isEmpty()) {
            if (!partResults.isEmpty()) {
                b.append(" [");
                b.append(partResults.get(0).getSolutions().size());
                for (int i = 1; i < partResults.size(); i++) {
                    b.append(", ").append(partResults.get(i).getSolutions().size());
                }
                b.append(']');
            }
            b.append(":\n");

            int i = 1;
            for (SolutionStatistics st : stats) {
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
        } else {
            int nbSolved = 0;
            for (SolvingStatistics x : partResults) {
                if (!x.getSolutions().isEmpty()) {
                    nbSolved++;
                }
            }
            b.append(": ").append(nbSolved).append('/').append(nbPartitions).append(" solved partition(s)");
        }
        return b.toString();
    }

    private void buildHeader(StringBuilder b, List<SolutionStatistics> stats) {
        b.append(nbNodes).append(" node(s)")
                .append("; ").append(nbVMs).append(" VM(s)");
        if (getNbManagedVMs() != nbVMs) {
            b.append(" (").append(getNbManagedVMs()).append(" managed)");
        }
        b.append("; ").append(nbWorkers).append(" worker(s)").append(", ").append(nbPartitions).append(" partition(s)");
        b.append("; ").append(nbConstraints).append(" constraint(s)");

        if (params.doOptimize()) {
            b.append("; optimize");
        }
        if (params.getTimeLimit() > 0) {
            b.append("; timeout: ").append(params.getTimeLimit()).append("s");
        }
        b.append("\nmax. building duration: ").append(getCoreRPBuildDuration()).append("ms (core-RP) + ")
                .append(getSpeRPDuration()).append("ms (specialization)");
        b.append("\nAfter ").append(getSolvingDuration()).append("ms of search");
        if (hitTimeout()) {
            b.append(" (timeout)");
        } else {
            b.append(" (terminated)");
        }
        b.append(": ")
                .append(nbSearchNodes).append(" opened search node(s), ")
                .append(nbBacktracks).append(" backtrack(s), ")
                .append(stats.size()).append(" solution(s)");

    }
}
