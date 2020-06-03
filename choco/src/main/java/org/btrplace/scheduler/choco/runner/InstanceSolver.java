/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.runner;

import org.btrplace.model.Instance;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;

/**
 * Interface to specify an instance solver.
 *
 * @author Fabien Hermenier
 */
public interface InstanceSolver {

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

    /**
     * Stop the solving process.
     */
    void stop();
}
