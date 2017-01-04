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

package org.btrplace.scheduler.choco.runner.single;

import org.btrplace.model.Instance;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.runner.Metrics;
import org.btrplace.scheduler.choco.runner.SolutionStatistics;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Statistics related to a solving process on one instance.
 *
 * @author Fabien Hermenier
 */
public class SingleRunnerStatistics implements SolvingStatistics {

    private Parameters params;

    private Instance instance;

    private long coreRPBuildDuration;

    private long speRPDuration;

    private long start;

    private boolean completed;

    /**
     * The number of VMs actually managed by the problem.
     */
    private int nbManagedVMs;

    private List<SolutionStatistics> solutions;

    private Metrics metrics;

    /**
     * Make new statistics.
     *
     * @param ps the scheduler parameters
     * @param i the instance to solve;
     * @param st         the moment the computation starts (epoch format)
     */
    public SingleRunnerStatistics(Parameters ps, Instance i, long st) {
        this.params = ps;
        this.start = st;
        solutions = new ArrayList<>();
        this.nbManagedVMs = -1;
        this.coreRPBuildDuration = -1;
        this.speRPDuration = -1;
        this.instance = i;
        metrics = null;
        completed = false;
    }

    @Override
    public long getCoreBuildDuration() {
        return coreRPBuildDuration;
    }

    /**
     * Set the scheduler generation duration.
     *
     * @param d an amount in milliseconds.
     */
    public void setCoreBuildDuration(long d) {
        coreRPBuildDuration = d;
    }

    /**
     * Set the scheduler specialisation duration.
     *
     * @param d an amount in milliseconds
     */
    public void setSpecialisationDuration(long d) {
        speRPDuration = d;
    }

    @Override
    public long getSpecializationDuration() {
        return speRPDuration;
    }

    /**
     * Add a solution to the statistics.
     *
     * @param so the solution to add
     */
    public void addSolution(SolutionStatistics so) {
        this.solutions.add(so);
    }

    @Override
    public Metrics getMetrics() {
        return this.metrics;
    }

    /**
     * Set the solver metrics.
     *
     * @param m the metrics
     */
    public void setMetrics(Metrics m) {
        this.metrics = m;
    }

    @Override
    public List<SolutionStatistics> getSolutions() {
        return solutions;
    }

    @Override
    public int getNbManagedVMs() {
        return nbManagedVMs;
    }

    /**
     * Set the number of VMs that are manageable by the scheduler
     *
     * @param nb a positive amount
     */
    public void setNbManagedVMs(int nb) {
        nbManagedVMs = nb;
    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        int nbNodes = instance.getModel().getMapping().getNbNodes();
        int nbVMs = instance.getModel().getMapping().getNbVMs();
        int nbConstraints = instance.getSatConstraints().size();
        b.append(nbNodes).append(" node(s)")
                .append("; ").append(nbVMs).append(" VM(s)");
        if (nbManagedVMs != nbVMs) {
            b.append(" (").append(nbManagedVMs).append(" managed)");
        }
        b.append("; ").append(nbConstraints).append(" constraint(s)");

        if (params.doOptimize()) {
            b.append("; optimize");
        }
        if (params.getTimeLimit() > 0) {
            b.append("; timeout: ").append(params.getTimeLimit()).append("s");
        }
        b.append("\nBuilding duration: ").append(coreRPBuildDuration).append("ms (core) + ").append(speRPDuration).append("ms (specialization)");
        b.append("\nAfter ").append(metrics.timeCount()).append("ms of search");

        if (completed) {
            b.append(" (terminated)");
        } else {
            b.append(" (timeout)");
        }

        b.append(": ")
                .append(metrics.toString())
                .append(", ")
                .append(solutions.size()).append(" solution(s)");
        if (!solutions.isEmpty()) {
            b.append(":\n");
        } else {
            b.append('.');
        }
        int i = 1;
        for (SolutionStatistics st : solutions) {
            b.append('\t').append(i).append(')').append(st.toString()).append("\n");
            i++;
        }
        return b.toString();
    }

    @Override
    public ReconfigurationPlan lastSolution() {
        if (solutions.isEmpty()) {
            return null;
        }
        return solutions.get(solutions.size() - 1).getReconfigurationPlan();
    }

    @Override
    public Parameters getParameters() {
        return params;
    }

    /**
     * Set the completion status.
     *
     * @param b {@code true} iff the search completed
     */
    public void setCompleted(boolean b) {
        completed = b;
    }

    @Override
    public boolean completed() {
        return completed;
    }


    @Override
    public String toCSV() {
        return String.format("%d;%d;%d;%d;%d;%d", nbManagedVMs,
                coreRPBuildDuration,
                speRPDuration,
                getMetrics().timeCount(),
                solutions.size(),
                completed ? 1 : 0);
    }
}

