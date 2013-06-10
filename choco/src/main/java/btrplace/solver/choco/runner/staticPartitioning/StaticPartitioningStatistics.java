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

package btrplace.solver.choco.runner.staticPartitioning;

import btrplace.solver.choco.ChocoReconfigurationAlgorithmParams;
import btrplace.solver.choco.runner.SolutionStatistics;
import btrplace.solver.choco.runner.SolvingStatistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Statistics for a solving process partitioned statically.
 *
 * @author Fabien Hermenier
 */
public class StaticPartitioningStatistics implements SolvingStatistics {

    private List<SolvingStatistics> partResults;

    private int nbNodes, nbVMs, nbConstraints, duration, nbManaged, coreRPDuration, speRPDuration;

    private int nbWorkers, nbSearchNodes, nbBacktracks, splitDuration, nbPartitions;

    private boolean hitTimeout;

    private ChocoReconfigurationAlgorithmParams params;

    /**
     * Make the statistics.
     *
     * @param ps            the standard parameters for the solving process
     * @param nbNodes       the number of nodes in the model
     * @param nbVMs         the number of VMs in the model
     * @param nbConstraints the number of satisfaction-oriented constraints.
     * @param splitDuration the duration of the splitting process in milliseconds
     * @param duration      the solving process duration in milliseconds
     * @param nbWorkers     the number of workers to solve the partitions in parallel
     * @param nbParts       the number of partitions to compute
     */
    public StaticPartitioningStatistics(ChocoReconfigurationAlgorithmParams ps, int nbNodes, int nbVMs, int nbConstraints,
                                        int splitDuration, int duration, int nbWorkers, int nbParts) {
        partResults = new ArrayList<>();
        this.nbNodes = nbNodes;
        this.nbVMs = nbVMs;
        this.nbConstraints = nbConstraints;
        this.duration = duration;
        nbSearchNodes = 0;
        nbBacktracks = 0;
        nbManaged = 0;
        coreRPDuration = 0;
        speRPDuration = 0;
        hitTimeout = false;
        this.nbWorkers = nbWorkers;
        params = ps;
        this.splitDuration = splitDuration;
        this.nbPartitions = nbParts;
    }

    @Override
    public int getSolvingDuration() {
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
    public int getNbSearchNodes() {
        return nbSearchNodes;
    }

    @Override
    public int getNbBacktracks() {
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

        int firstN = 0, firstB = 0, firstOptValue = 0, firstTime = 0;
        int lastN = 0, lastB = 0, lastOptValue = 0, lastTime = 0;
        for (SolvingStatistics st : partResults) {
            if (st.getSolutions().isEmpty()) { //At least 1 partition does not have a result.
                return solutions;
            } else {
                SolutionStatistics first = st.getSolutions().get(0);
                firstN += first.getNbNodes();
                firstB += first.getNbBacktracks();
                firstOptValue += first.getOptValue();

                if (st.getSolutions().size() > 1) {
                    SolutionStatistics last = st.getSolutions().get(st.getSolutions().size());
                    lastN += last.getNbNodes();
                    lastB += last.getNbBacktracks();
                    lastOptValue += last.getOptValue();
                }
            }
        }

        solutions.add(new SolutionStatistics(firstN, firstB, firstTime, firstOptValue));
        if (lastOptValue != firstOptValue) {
            solutions.add(new SolutionStatistics(lastN, lastB, lastTime, lastOptValue));
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
    public ChocoReconfigurationAlgorithmParams getParameters() {
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
        speRPDuration = (int) Math.max(speRPDuration, stats.getCoreRPBuildDuration());
        partResults.add(stats);
    }

    /**
     * Get the partition splitting duration in milliseconds.
     *
     * @return a positive value.
     */
    public int getSplitDuration() {
        return splitDuration;
    }


}
