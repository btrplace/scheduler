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

import org.btrplace.plan.ReconfigurationPlan;

/**
 * Store statistics about a solution.
 *
 * @author Fabien Hermenier
 */
public class SolutionStatistics {


    private ReconfigurationPlan solution = null;

    private Metrics measures;

    private boolean hasObjective;

    private int objective;

    /**
     * Make a new statistics.
     *
     * @param m the solver metrics at the moment of the solution
     * @param  plan the resulting plan. {@code null} indicates the solver stated their is no solution
     */
    public SolutionStatistics(Metrics m, ReconfigurationPlan plan) {
        measures = m;
        solution = plan;
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
