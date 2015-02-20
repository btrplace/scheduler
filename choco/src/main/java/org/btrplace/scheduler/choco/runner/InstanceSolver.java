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

import org.btrplace.model.Instance;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface to specify an instance solver.
 *
 * @author Fabien Hermenier
 */
public interface InstanceSolver {

    Logger LOGGER = LoggerFactory.getLogger("StaticPartitioning");

    /**
     * Solve an instance.
     *
     * @param ps the parameters to consider
     * @param i  the instance to solve
     * @return the resulting reconfiguration plan, {@code null} if there is no solution
     * @throws SchedulerException if an error prevent from running a solving process
     */
    ReconfigurationPlan solve(Parameters ps,
                         Instance i) throws SchedulerException;

    /**
     * Return the statistics of the solving process.
     * @return some statistics
     * @throws SchedulerException if an error occurred
     */
    SolvingStatistics getStatistics() throws SchedulerException;
}
