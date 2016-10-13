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

package org.btrplace.scheduler.choco;

import org.btrplace.scheduler.Scheduler;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.runner.InstanceSolver;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;

/**
 * A scheduler based on the Choco constraint solver.
 *
 * @author Fabien Hermenier
 */
public interface ChocoScheduler extends Scheduler, Parameters {

    /**
     * Get statistics about the last solved problem.
     *
     * @throws SchedulerException if an error occurred
     * @return some statistics, {@code null} if no problem has been solved for the moment
     */
    SolvingStatistics getStatistics() throws SchedulerException;

    /**
     * Get the solver used to solve a problem.
     *
     * @return the current used solver
     */
    InstanceSolver getInstanceSolver();

    /**
     * Set the solver to use to solve a problem.
     *
     * @param p the runner to use
     */
    void setInstanceSolver(InstanceSolver p);

    /**
     * Set the scheduler parameters.
     *
     * @param ps the parameters
     * @return this
     */
    ChocoScheduler setParameters(Parameters ps);

    /**
     * Get the scheduler parameters.
     *
     * @return the registered parameters
     */
    Parameters getParameters();
}
