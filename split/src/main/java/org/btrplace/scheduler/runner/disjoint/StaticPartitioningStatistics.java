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

package org.btrplace.scheduler.runner.disjoint;

import org.btrplace.model.Instance;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.runner.Metrics;
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

    private int nbWorkers;
    private int nbPartitions;
    private long splitDuration;
    private long solvingDuration;
    private int managed = 0;

    private Instance instance;
    private Parameters params;
    private long start;
    private boolean completed = false;

    private long core = -1;
    private long spe = -1;

    /**
     * Make the statistics.
     *
     * @param ps the scheduler parameters
     * @param i the instance to solve;
     * @param st      the moment the computation starts (epoch format)
     * @param w       the number of workers to solve the partitions in parallel
     */
    public StaticPartitioningStatistics(Parameters ps, Instance i, long st, int w) {
        instance = i;
        params = ps;
        start = st;
        this.nbWorkers = w;
        this.splitDuration = -1;
        this.nbPartitions = -1;
        solvingDuration = -1;
        partResults = new ArrayList<>();
    }

    /**
     * Set statistics about the splitting process.
     *
     * @param nbParts the number of
     * @param d
     */
    public void setSplittingStatistics(int nbParts, long d) {
        splitDuration = d;
        nbPartitions = nbParts;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    @Override
    public long getCoreBuildDuration() {
        return core;
    }

    @Override
    public long getSpecializationDuration() {
        return spe;
    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public int getNbManagedVMs() {
        return managed;
    }

    @Override
    public Parameters getParameters() {
        return params;
    }

    @Override
    public Metrics getMetrics() {
        return null;
    }

    @Override
    public boolean completed() {
        return completed;
    }

    @Override
    public ReconfigurationPlan lastSolution() {
        return null;
    }

    /**
     * Get the solving duration in milliseconds
     *
     * @return a positive number. {@code -1} if the solving process did not start
     */
    public long getSolvingDuration() {
        return solvingDuration;
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
/*
        int firstN = 0;
        int firstB = 0;
        int firstOptValue = 0;
        int lastN = 0;
        int lastB = 0;
        int lastOptValue = 0;

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
            }

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

        solutions.add(new SolutionStatistics(firstN, firstB, endFirst - start, firstOptValue));
        if (multipleSolution) {
            solutions.add(new SolutionStatistics(lastN, lastB, endLast - start, lastOptValue));
        }*/
        return solutions;
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
        /*
        nbBacktracks += stats.getNbBacktracks();
        nbSearchNodes += stats.getNbSearchNodes();
        nbManaged += stats.getNbManagedVMs();
        hitTimeout |= stats.hitTimeout();
        coreRPDuration = (int) Math.max(coreRPDuration, stats.getCoreBuildDuration());
        speRPDuration = (int) Math.max(speRPDuration, stats.getSpecializationDuration());
        */
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
    public String toString() {
        StringBuilder b = new StringBuilder();
/*
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
        }*/
        return b.toString();
    }

    @Override
    public String toCSV() {
        throw new UnsupportedOperationException();
    }

    public void setSolvingDuration(long solvingDuration) {
        this.solvingDuration = solvingDuration;
    }

    /*private void buildHeader(StringBuilder b, List<SolutionStatistics> stats) {
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
        b.append("\nmax. building duration: ").append(getCoreBuildDuration()).append("ms (core-RP) + ")
                .append(getSpecializationDuration()).append("ms (specialization)");
        b.append("\nAfter ").append(getSolvingDuration()).append("ms of search");
        if (completed()) {
            b.append(" (terminated)");
        } else {
            b.append(" (timeout)");
        }
        b.append(": ")
                .append(nbSearchNodes).append(" opened search node(s), ")
                .append(nbBacktracks).append(" backtrack(s), ")
                .append(stats.size()).append(" solution(s)");

    }*/
}
