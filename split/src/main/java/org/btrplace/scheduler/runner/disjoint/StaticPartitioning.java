/*
 * Copyright (c) 2018 University Nice Sophia Antipolis
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
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.runner.InstanceSolver;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;
import org.btrplace.scheduler.choco.runner.single.InstanceSolverRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An abstract solver that decompose statically an instance
 * into multiple disjoint sub-instances than are solved in parallel.
 * <p>
 * The resulting reconfiguration plan is composed by all the sub
 * reconfiguration plans. Each sub-instance must then have a solution.
 * <p>
 * The solving process relies on a master/worker paradigm with a number
 * of workers equals to the number of available cores by default.
 *
 * @author Fabien Hermenier
 */
public abstract class StaticPartitioning implements InstanceSolver {

    private int workersCount;

    private StaticPartitioningStatistics stats;

    private List<InstanceSolverRunner> runners;

    /**
     * Make a new partitioning algorithm.
     * The number of workers is set to the number of available cores.
     */
    public StaticPartitioning() {
        workersCount = Runtime.getRuntime().availableProcessors();
    }

    /**
     * Get the number of workers that are used to solve instances.
     *
     * @return a number &gt;= 1
     */
    public int getWorkersCount() {
        return workersCount;
    }

    /**
     * Set the number of workers that solve instances.
     *
     * @param s a number &gt;= 1
     */
    public void setWorkersCount(int s) {
        this.workersCount = s;
    }


    @Override
    public ReconfigurationPlan solve(Parameters cra, Instance orig) throws SchedulerException {
        stats = new StaticPartitioningStatistics(cra, orig, System.currentTimeMillis(), workersCount);
        long d = -System.currentTimeMillis();
        List<Instance> partitions = split(cra, orig);
        d += System.currentTimeMillis();

        stats.setSplittingStatistics(partitions.size(), d);
        ExecutorService exe = Executors.newFixedThreadPool(this.workersCount);
        CompletionService<SolvingStatistics> completionService = new ExecutorCompletionService<>(exe);
        List<SolvingStatistics> results = new ArrayList<>(partitions.size());


        runners = new ArrayList<>();
        long duration = -System.currentTimeMillis();
        for (Instance partition : partitions) {
            InstanceSolverRunner runner = new InstanceSolverRunner(cra, partition);
            completionService.submit(runner);
            runners.add(runner);
        }

        for (int i = 0; i < partitions.size(); i++) {
            try {
                results.add(completionService.take().get());
            } catch (ExecutionException ignore) {
                Throwable cause = ignore.getCause();
                if (cause != null) {
                    throw new SplitException(null, cause.getMessage(), ignore);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SplitException(orig.getModel(), e.getMessage(), e);
            }
        }
        duration += System.currentTimeMillis();
        stats.setSolvingDuration(duration);
        exe.shutdown();

        return merge(orig, results);
    }

    private ReconfigurationPlan merge(Instance i, Collection<SolvingStatistics> results) throws SplitException {
        ReconfigurationPlan plan = new DefaultReconfigurationPlan(i.getModel());
        //Only if there is a solution
        for (SolvingStatistics result : results) {
            getStatistics().addPartitionStatistics(result);
            ReconfigurationPlan p = result.lastSolution();
            if (p == null) {
                return null;
            }
            for (Action a : p) {
                    if (!plan.add(a)) {
                        throw new SplitException(plan.getOrigin(),
                                "Unable to add action '" + a + "' while merging the sub-plans");
                    }
                }
        }
        return plan;
    }

    @Override
    public StaticPartitioningStatistics getStatistics() {
        return stats;
    }

    /**
     * Split an instance into several disjoint instances.
     *
     * @param ps the parameters for the solver
     * @param i  the instance to split
     * @return a list of disjoint instances. Cannot be empty.
     * @throws org.btrplace.scheduler.SchedulerException if an error prevent the splitting process
     */
    public abstract List<Instance> split(Parameters ps, Instance i) throws SchedulerException;

    @Override
    public void stop() {
        if (runners != null) {
            for (InstanceSolverRunner runner: runners) {
                runner.stop();
            }
        }
    }
}
