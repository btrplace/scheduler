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

import org.btrplace.scheduler.choco.Parameters;

import java.util.ArrayList;
import java.util.List;

/**
 * Statistics to aggregate the statistics of a multi-stage resolution.
 *
 * @author Fabien Hermenier
 */
public class StagedSolvingStatistics implements SolvingStatistics {

    private List<SolvingStatistics> stages;

    /**
     * Make a new statistic.
     *
     */
    public StagedSolvingStatistics() {
        stages = new ArrayList<>();
    }


    public StagedSolvingStatistics append(SolvingStatistics stats) {
        stages.add(stats);
        return this;
    }

    private SolvingStatistics last() {
        return stages.get(stages.size() - 1);
    }

    private SolvingStatistics first() {
        return stages.get(0);
    }

    public int getNbStages() {
        return stages.size();
    }

    public SolvingStatistics getAtState(int st) {
        return stages.get(st);
    }

    @Override
    public int getNbConstraints() {
        return last().getNbConstraints();
    }

    /**
     * Return the aggregated solving duration.
     *
     * @return a long
     */
    @Override
    public long getSolvingDuration() {
        return stages.stream().mapToLong(SolvingStatistics::getSolvingDuration).sum();
    }

    /**
     * Return the aggregated core problem build duration.
     *
     * @return a long
     */
    @Override
    public long getCoreRPBuildDuration() {
        return stages.stream().mapToLong(SolvingStatistics::getCoreRPBuildDuration).sum();
    }

    /**
     * Return the aggregated specialisation duration.
     *
     * @return a long
     */
    @Override
    public long getSpeRPDuration() {
        return stages.stream().mapToLong(SolvingStatistics::getSpeRPDuration).sum();
    }

    /**
     * Return the timestamp of the first phase.
     *
     * @return a timestamp
     */
    @Override
    public long getStart() {
        return first().getStart();
    }

    /**
     * Return the aggregated number of search nodes.
     *
     * @return a long
     */
    @Override
    public long getNbSearchNodes() {
        return stages.stream().mapToLong(SolvingStatistics::getNbSearchNodes).sum();
    }

    /**
     * Return the aggregated number of backtracks.
     *
     * @return a long
     */
    @Override
    public long getNbBacktracks() {
        return stages.stream().mapToLong(SolvingStatistics::getNbBacktracks).sum();
    }

    /**
     * Return the status of the last stage.
     */
    @Override
    public boolean hitTimeout() {
        return last().hitTimeout();
    }

    /**
     * Return all the statistics.
     *
     * @return a list that might be empty
     */
    @Override
    public List<SolutionStatistics> getSolutions() {
        return last().getSolutions();
    }

    @Override
    public int getNbVMs() {
        return last().getNbConstraints();
    }

    @Override
    public int getNbNodes() {
        return last().getNbConstraints();
    }

    @Override
    public int getNbManagedVMs() {
        return last().getNbConstraints();
    }

    @Override
    public Parameters getParameters() {
        return first().getParameters();
    }

    /**
     * Print each stage statistics
     *
     * @return a formatted string
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        int i = 1;
        for (SolvingStatistics st : stages) {
            b.append("---- Stage ").append(i).append("/").append(stages.size()).append(" ----\n");
            b.append(st.toString()).append("\n");
            i++;
        }
        return b.toString();
    }
}
