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

package org.btrplace.scheduler;

import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.OptConstraint;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.plan.ReconfigurationPlan;

import java.util.Collection;

/**
 * Basic interface for a VM scheduler.
 *
 * @author Fabien Hermenier
 */
public interface Scheduler {

    /**
     * Compute a reconfiguration plan to reach a solution to the model
     *
     * @param i     the current model
     * @param cstrs the satisfaction-oriented constraints that must be considered
     * @param obj   the optimization-oriented constraint that must be considered
     * @return the plan to execute to reach the new solution or {@code null} if there is no
     * solution.
     * @throws SchedulerException if an error occurred while trying to solve the problem
     */
    ReconfigurationPlan solve(Model i, Collection<SatConstraint> cstrs, OptConstraint obj) throws SchedulerException;

    /**
     * Compute a reconfiguration plan to reach a solution to an instance
     *
     * @param i the instance to solve
     * @return the plan to execute to reach the new solution or {@code null} if there is no
     * solution.
     * @throws SchedulerException if an error occurred while trying to solve the problem
     */
    ReconfigurationPlan solve(Instance i) throws SchedulerException;
}
