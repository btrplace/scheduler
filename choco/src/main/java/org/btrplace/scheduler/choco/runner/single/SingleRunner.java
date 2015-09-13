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
        return r.call().getPlan();
    }

    @Override
    public SolvingStatistics getStatistics() throws SchedulerException {
        if (r == null) {
            return null;
        }
        return r.getStatistics();
    }
}
