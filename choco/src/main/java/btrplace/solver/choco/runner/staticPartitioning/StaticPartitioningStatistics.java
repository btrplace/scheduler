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
 * @author Fabien Hermenier
 */
public class StaticPartitioningStatistics implements SolvingStatistics {

    private List<SolvingStatistics> partResults;

    private int nbNodes, nbVMs, nbConstraints, duration, nbSearchNodes, nbBacktracks, nbManaged, coreRPDuration, speRPDuration;

    private int nbWorkers;

    private boolean hitTimeout;

    private int splitDuration;

    private ChocoReconfigurationAlgorithmParams params;

    public StaticPartitioningStatistics(ChocoReconfigurationAlgorithmParams ps, int nbNodes, int nbVMs, int nbConstraints, int splitDuration, int duration, int nbWorkers) {
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

    @Override
    public List<SolutionStatistics> getSolutions() {
        throw new UnsupportedOperationException();
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

    public int getNbParts() {
        return partResults.size();
    }

    public int getNbWorkers() {
        return nbWorkers;
    }

    public void addPartitionStatistics(SolvingStatistics stats) {
        nbBacktracks += stats.getNbBacktracks();
        nbSearchNodes += stats.getNbSearchNodes();
        nbManaged += stats.getNbManagedVMs();
        hitTimeout |= stats.hitTimeout();
        coreRPDuration = (int) Math.max(coreRPDuration, stats.getCoreRPBuildDuration());
        speRPDuration = (int) Math.max(speRPDuration, stats.getCoreRPBuildDuration());
        partResults.add(stats);
    }

    public int getSplitDuration() {
        return splitDuration;
    }


}
