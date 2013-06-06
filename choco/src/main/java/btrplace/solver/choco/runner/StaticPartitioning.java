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

package btrplace.solver.choco.runner;

import btrplace.model.Instance;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithmParams;

import java.util.List;
import java.util.concurrent.*;

/**
 * An abstract solver that decompose an instance into multiple disjoint models.
 * The method to split the instances must be developed. The computed partitions will
 * be solved in parallel with a controlled amount of simultaneous workers.
 * <p/>
 * By default, the number of workers equals the number of available cores.
 *
 * @author Fabien Hermenier
 */
public abstract class StaticPartitioning implements InstanceSolver {

    private int workersCount;

    /**
     * Get the number of workers that are used to solve instances.
     *
     * @return a number >= 1
     */
    public int getWorkersCount() {
        return workersCount;
    }

    /**
     * Set the number of workers that solve instances.
     *
     * @param s a number >= 1
     */
    public void setWorkersCount(int s) {
        this.workersCount = s;
    }

    /**
     * Make a new partitioner.
     * The number of workers is set to the number of available cores.
     */
    public StaticPartitioning() {
        workersCount = Runtime.getRuntime().availableProcessors();
    }

    @Override
    public InstanceResult solve(ChocoReconfigurationAlgorithmParams cra, Instance instance) throws SolverException {
        List<Instance> partitions = split(cra, instance);

        ExecutorService exe = Executors.newFixedThreadPool(this.workersCount);
        CompletionService<InstanceResult> completionService = new ExecutorCompletionService<>(exe);
        for (Instance partition : partitions) {
            completionService.submit(new InstanceSolverRunner(cra, partition));
        }

        for (int i = 0; i < partitions.size(); ++i) {
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
        //TODO: merge the statistics and the reconfiguration plans
        return null;

    }

    /**
     * Split an instance into several disjoint instances.
     *
     * @param ps the parameters for the solver
     * @param i  the instance to split
     * @return a list of disjoint instances. Cannot be empty.
     * @throws SolverException if an error prevent the spliting process
     */
    public abstract List<Instance> split(ChocoReconfigurationAlgorithmParams ps, Instance i) throws SolverException;


}
