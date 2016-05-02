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
import org.chocosolver.solver.search.measure.IMeasures;

/**
 * Store statistics about a solution.
 *
 * @author Fabien Hermenier
 */
public class SolutionStatistics {


    private ReconfigurationPlan solution = null;

    private IMeasures measures;

    /**
     * Make a new statistics.
     *
     * @param m the solver measures at the moment of the solution
     * @param  plan the resulting plan. {@code null} indicates the solver stated their is no solution
     */
    public SolutionStatistics(IMeasures m, ReconfigurationPlan plan) {
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
     * Return the solver measures at the moment the solution was computed.
     * @return solver measurement
     */
    public IMeasures getMeasures() {
        return measures;
    }

    @Override
    public String toString() {
        String res = String.format("at %dms, %d node(s), %d backtrack(s)",
                (int) (measures.getTimeCount() * 1000),
                measures.getNodeCount(),
                measures.getBackTrackCount());

        if (measures.hasObjective()) {
            res = String.join("", res, ", objective: ", measures.getBestSolutionValue().toString());
        }
        return res;
    }
}
