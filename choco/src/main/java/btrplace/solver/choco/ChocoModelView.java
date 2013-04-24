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

package btrplace.solver.choco;

import btrplace.plan.ReconfigurationPlan;

/**
 * Interface denoting the Choco implementation of a {@link btrplace.model.ModelView}.
 *
 * @author Fabien Hermenier
 */
public interface ChocoModelView {

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
     */
    boolean beforeSolve(ReconfigurationProblem rp);

    /**
     * Allow the insertion of actions on the plan computed for a given problem.
     *
     * @param rp the solver problem
     * @param p  the computed plan
     * @return {@code true} iff the insertion succeeded
     */
    boolean insertActions(ReconfigurationProblem rp, ReconfigurationPlan p);
}
