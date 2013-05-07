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

package btrplace.solver.choco.objective;

import btrplace.solver.SolverException;
import btrplace.solver.choco.MisplacedVMsEstimator;
import btrplace.solver.choco.ReconfigurationProblem;

/**
 * An objective for the reconfiguration algorithm.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationObjective extends MisplacedVMsEstimator {

    /**
     * Inject the objective into a given problem
     *
     * @param rp the problem to inject the objective into
     * @throws SolverException if an error occurs.
     */
    void inject(ReconfigurationProblem rp) throws SolverException;
}
