/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

    /**
     * Stop the solving process.
     */
    void stop();
}
