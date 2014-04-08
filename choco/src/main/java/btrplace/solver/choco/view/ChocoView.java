/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.solver.choco.view;

import btrplace.model.VM;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ReconfigurationProblem;


/**
 * Interface denoting the Choco implementation of a View. Such a view might be
 * generated from a {@link btrplace.model.view.ModelView} thanks to the {@link btrplace.solver.choco.view.ChocoModelViewBuilder}
 * or might be a solver-only view provided through {@link btrplace.solver.choco.ChocoReconfigurationAlgorithmParams}.
 *
 * @author Fabien Hermenier
 */
public interface ChocoView {

    /**
     * Get the view unique identifier.
     *
     * @return a non-empty String
     */
    String getIdentifier();

    /**
     * An event that is send to indicate a RP will be solved.
     * The view can then customize the RP a last time.
     *
     * @return {@code false} iff there will be no solution to the RP.
     * @throws btrplace.solver.SolverException if an error occurred while building the problem
     */
    boolean beforeSolve(ReconfigurationProblem rp) throws SolverException;

    /**
     * Allow the insertion of actions on the plan computed for a given problem.
     *
     * @param rp the solver problem
     * @param p  the computed plan
     * @return {@code true} iff the insertion succeeded
     */
    boolean insertActions(ReconfigurationProblem rp, ReconfigurationPlan p);

    /**
     * Notify a new VM will be a clone of an already known VM.
     *
     * @param vm    the old VM that will be substituted by the clone
     * @param clone the clone identifier
     * @return {@code true} iff the view validate the cloning process.
     */
    boolean cloneVM(VM vm, VM clone);
}
