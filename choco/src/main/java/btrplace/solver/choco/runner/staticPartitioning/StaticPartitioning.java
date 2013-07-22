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

import btrplace.model.Instance;
import btrplace.model.Mapping;
import btrplace.model.Node;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.Action;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithmParams;
import btrplace.solver.choco.runner.InstanceResult;
import btrplace.solver.choco.runner.InstanceSolver;
import btrplace.solver.choco.runner.SolvingStatistics;
import btrplace.solver.choco.runner.single.InstanceSolverRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * An abstract solver that decomposes an instance into multiple disjoint models.
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
    public InstanceResult solve(ChocoReconfigurationAlgorithmParams cra, Instance orig) throws SolverException {
        long start = System.currentTimeMillis();
        long splitDuration = -System.currentTimeMillis();
        List<Instance> partitions = split(cra, orig);
        splitDuration += System.currentTimeMillis();

        ExecutorService exe = Executors.newFixedThreadPool(this.workersCount);
        CompletionService<InstanceResult> completionService = new ExecutorCompletionService<>(exe);
        List<InstanceResult> results = new ArrayList<>(partitions.size());

        int nbVMs = 0;
        Mapping origMapping = orig.getModel().getMapping();
        for (Node n : origMapping.getOnlineNodes()) {
            nbVMs += origMapping.getRunningVMs(n).size();
            nbVMs += origMapping.getSleepingVMs(n).size();
        }
        nbVMs += origMapping.getReadyVMs().size();
        int nbNodes = origMapping.getOnlineNodes().size() + origMapping.getOfflineNodes().size();
        int nbConstraints = orig.getConstraints().size();

        long duration = -System.currentTimeMillis();
        for (Instance partition : partitions) {
            completionService.submit(new InstanceSolverRunner(cra, partition));
        }

        for (int i = 0; i < partitions.size(); i++) {
            try {
                results.add(completionService.take().get());
            } catch (ExecutionException ignore) {
                Throwable cause = ignore.getCause();
                if (cause != null) {
                    throw new SolverException(null, "", cause);
                }
            } catch (InterruptedException e) {
                throw new SolverException(orig.getModel(), e.getMessage(), e);
            }
        }
        duration += System.currentTimeMillis();
        StaticPartitioningStatistics stats = new StaticPartitioningStatistics(cra, nbNodes,
                nbVMs,
                nbConstraints,
                start,
                splitDuration,
                duration,
                workersCount,
                partitions.size()
        );

        exe.shutdown();

        InstanceResult res = new InstanceResult(new DefaultReconfigurationPlan(orig.getModel()), stats);
        merge(res, results);
        return res;
    }

    private void merge(InstanceResult merged, Collection<InstanceResult> results) throws SolverException {
        ReconfigurationPlan plan = merged.getPlan();
        for (InstanceResult result : results) {
            for (Action a : result.getPlan()) {
                if (!plan.add(a)) {
                    logger.error("Unable to add action {} while merging the results", a);
                }
            }
            SolvingStatistics st = result.getStatistics();
            ((StaticPartitioningStatistics) merged.getStatistics()).addPartitionStatistics(st);
        }
    }

    /**
     * Split an instance into several disjoint instances.
     *
     * @param ps the parameters for the solver
     * @param i  the instance to split
     * @return a list of disjoint instances. Cannot be empty.
     * @throws SolverException if an error prevent the splitting process
     */
    public abstract List<Instance> split(ChocoReconfigurationAlgorithmParams ps, Instance i) throws SolverException;
}
