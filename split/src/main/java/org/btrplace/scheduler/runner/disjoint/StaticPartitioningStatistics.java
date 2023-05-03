/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

  private final List<SolvingStatistics> partResults;

  private final int nbWorkers;
  private int nbPartitions;
  private long splitDuration;
  private long solvingDuration;
  private final int managed = 0;

  private final Instance instance;
  private final Parameters params;
  private final long start;
  private final boolean completed = false;

  private final long core = -1;
  private final long spe = -1;

  /**
   * Make the statistics.
   *
   * @param ps the scheduler parameters
   * @param i  the instance to solve;
   * @param st the moment the computation starts (epoch format)
   * @param w  the number of workers to solve the partitions in parallel
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
     * @param nbParts the number of partitions
     * @param d the partitioning duration
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
        return new ArrayList<>();
    }

    /**
     * Get the number of partitions.
     *
     * @return a number &gt;= 1
     */
    public int getNbParts() {
        return nbPartitions;
    }

    /**
     * Get the maximum number of workers to that works in parallel
     *
     * @return a number &gt;= 1
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
        partResults.add(stats);
    }

  /**
   * Return the results per partition.
   *
   * @return a collection of results that may be empty.
   */
  public List<SolvingStatistics> results() {
    return partResults;
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
        return b.toString();
    }

    @Override
    public String toCSV() {
        throw new UnsupportedOperationException();
    }

    public void setSolvingDuration(long solvingDuration) {
        this.solvingDuration = solvingDuration;
    }
}
