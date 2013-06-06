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
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithmParams;

import java.util.Collection;
import java.util.concurrent.*;

/**
 * @author Fabien Hermenier
 */
public class ParallelIndependentInstancesSolver implements InstancesSolver {

    private int workersCount;

    public int getWorkersCount() {
        return workersCount;
    }

    public void setWorkersCount(int s) {
        this.workersCount = s;
    }

    @Override
    public InstanceResult solve(ChocoReconfigurationAlgorithmParams params, Collection<Instance> instances) throws SolverException {
        ExecutorService exe = Executors.newFixedThreadPool(this.workersCount);
        CompletionService<InstanceResult> completionService = new ExecutorCompletionService<>(exe);
        for (Instance i : instances) {
            completionService.submit(new InstanceSolverRunner(params, i));
        }

        for (int i = 0; i < instances.size(); ++i) {
            try {
                InstanceResult res = completionService.take().get();
                if (res != null) {
                    System.out.println(res);
                }
            } catch (ExecutionException ignore) {
                Throwable cause = ignore.getCause();
                if (cause != null) {
                    throw new SolverException(null, "", cause);
                }
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }

        exe.shutdown();
        return null;
    }
}
