/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver;

import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.plan.ReconfigurationPlan;

import java.util.Collection;

/**
 * Basic interface for a reconfiguration algorithm.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationAlgorithm {

    /**
     * Compute a reconfiguration plan to reach a solution to the model
     *
     * @param i     the current model
     * @param cstrs the constraints that must be considered
     * @return the plan to execute to reach the new solution or {@code null} if there is no
     *         solution.
     * @throws SolverException if an error occurred while trying to solve the problem
     */
    ReconfigurationPlan solve(Model i, Collection<SatConstraint> cstrs) throws SolverException;
}
