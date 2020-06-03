/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import org.btrplace.scheduler.SchedulerException;

/**
 * Interface to specify an object that can be injected inside a reconfiguration problem.
 *
 * @author Fabien Hermenier
 */
public interface Injectable {

    /**
     * Inject the constraint into the problem.
     *
     * @param ps the scheduler parameters
     * @param rp the problem
     * @return {@code true} if the injection succeeded, {@code false} if the problem is sure to not have a solution
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred while injecting.
     */
    default boolean inject(Parameters ps, ReconfigurationProblem rp) throws SchedulerException {
        return true;
    }
}
