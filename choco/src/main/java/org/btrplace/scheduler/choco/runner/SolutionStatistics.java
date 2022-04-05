/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.runner;

import org.btrplace.plan.ReconfigurationPlan;

/**
 * Store statistics about a solution.
 *
 * @author Fabien Hermenier
 */
public class SolutionStatistics {


    private ReconfigurationPlan solution = null;

  private final Metrics measures;

    private boolean hasObjective;

    private int objective;

    /**
     * Make a new statistics.
     *
     * @param m the solver metrics at the moment of the solution
     * @param  plan the resulting plan. {@code null} indicates the solver stated their is no solution
     */
    public SolutionStatistics(Metrics m, ReconfigurationPlan plan) {
        this(m);
        solution = plan;
    }

    public SolutionStatistics(Metrics m) {
        this.measures = m;
    }

    /**
     * Return the computed solution.
     * @return a plan that might be null
     */
    public ReconfigurationPlan getReconfigurationPlan() {
        return solution;
    }

    /**
     * Return the solver metrics at the moment the solution was computed.
     * @return solver measurement
     */
    public Metrics getMetrics() {
        return measures;
    }

    /**
     * Set the objective value associated to the solution
     *
     * @param v the value
     */
    public void setObjective(int v) {
        hasObjective = true;
        objective = v;
    }

    public void setReconfigurationPlan(final ReconfigurationPlan plan) {
        this.solution = plan;
    }

    /**
     * Indicates if an objective is attached to the solution
     *
     * @return {@code true} iff there is an attached objective
     */
    public boolean hasObjective() {
        return hasObjective;
    }

    /**
     * Get the objective value for that solution.
     * The value is meaningful iff there is an objective.
     *
     * @return a number
     */
    public int objective() {
        return objective;
    }

    @Override
    public String toString() {
        String res = measures.toString();
        if (hasObjective) {
            res = res + ", objective: " + objective;
        }
        return res;
    }
}
