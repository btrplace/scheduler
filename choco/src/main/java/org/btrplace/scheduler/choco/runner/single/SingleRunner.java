/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.runner.single;

import org.btrplace.model.Instance;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.runner.InstanceSolver;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;

/**
 * A simple runner that solve in one stage a whole instance.
 *
 * @author Fabien Hermenier
 */
public class SingleRunner implements InstanceSolver {

    private InstanceSolverRunner r;
    @Override
    public ReconfigurationPlan solve(Parameters cra,
                                Instance i) throws SchedulerException {
        r = new InstanceSolverRunner(cra, i);
        return r.call().lastSolution();
    }

    @Override
    public SolvingStatistics getStatistics() throws SchedulerException {
        if (r == null) {
            return null;
        }
        return r.getStatistics();
    }

    @Override
    public void stop() {
        if (r != null) {
            r.stop();
        }
    }
}
