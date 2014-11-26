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

package org.btrplace.scheduler.choco.constraint;

import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.MisplacedVMsEstimator;
import org.btrplace.scheduler.choco.ReconfigurationProblem;

/**
 * An interface to describe a constraint implementation for the solver.
 *
 * @author Fabien Hermenier
 */
public interface ChocoConstraint extends MisplacedVMsEstimator {

    /**
     * Inject the constraint into the problem.
     *
     * @param rp the problem
     * @return {@code true} if the injection succeeded, {@code false} if the problem is sure to not have a solution
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred while injecting.
     */
    boolean inject(ReconfigurationProblem rp) throws SchedulerException;
}
