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

import org.btrplace.model.Instance;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.Parameters;
import org.chocosolver.solver.search.measure.IMeasures;

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

    /**
     * Return the aggregated core problem build duration.
     *
     * @return a long
     */
    @Override
    public long getCoreBuildDuration() {
        return stages.stream().mapToLong(SolvingStatistics::getCoreBuildDuration).sum();
    }

    /**
     * Return the aggregated specialisation duration.
     *
     * @return a long
     */
    @Override
    public long getSpecializationDuration() {
        return stages.stream().mapToLong(SolvingStatistics::getSpecializationDuration).sum();
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
     * Return all the statistics.
     *
     * @return a list that might be empty
     */
    @Override
    public List<SolutionStatistics> getSolutions() {
        return last().getSolutions();
    }

    @Override
    public int getNbManagedVMs() {
        return last().getNbManagedVMs();
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

    @Override
    public IMeasures getMeasures() {
        return last().getMeasures();
    }

    @Override
    public Instance getInstance() {
        return first().getInstance();
    }

    @Override
    public boolean completed() {
        return last().completed();
    }

    @Override
    public ReconfigurationPlan lastSolution() {
        return last().lastSolution();
    }

    @Override
    public String toCSV() {
        throw new UnsupportedOperationException();
    }
}
