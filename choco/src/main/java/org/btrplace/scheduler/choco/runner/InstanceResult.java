/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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
 * Store the result of a solving process made by a runner.
 *
 * @author Fabien Hermenier
 */
public class InstanceResult {

    private ReconfigurationPlan plan;

    private SolvingStatistics stats;

    /**
     * Make a new result.
     *
     * @param p  the computed reconfiguration plan
     * @param st the statistics associated to the solving process
     */
    public InstanceResult(ReconfigurationPlan p, SolvingStatistics st) {
        plan = p;
        stats = st;
    }

    /**
     * Get the resulting reconfiguration plan.
     *
     * @return a plan. {@code null} if the runner found there was no solution.
     */
    public ReconfigurationPlan getPlan() {
        return plan;
    }

    /**
     * Get the statistics associated to the solving process.
     *
     * @return some statistics
     */
    public SolvingStatistics getStatistics() {
        return stats;
    }
}
