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

package btrplace.solver.choco.partitioner;

import btrplace.model.Instance;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.SolvingStatistics;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Fabien Hermenier
 */
public class IndependentInstancesSolver implements InstancesSolver {

    private int workersCount;

    public int getWorkersCount() {
        return workersCount;
    }

    public void setWorkersCount(int s) {
        this.workersCount = s;
    }

    @Override
    public ReconfigurationPlan solve(ChocoReconfigurationAlgorithm cra, Collection<Instance> instances) {
        Executor exe = Executors.newFixedThreadPool(this.workersCount);
        for (Instance i : instances) {
            exe.execute(new InstanceSolverRunner(cra, i));
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public SolvingStatistics getSolvingStatistics() {
        throw new UnsupportedOperationException();
    }
}
