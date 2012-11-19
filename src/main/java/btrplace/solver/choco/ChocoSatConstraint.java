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

import btrplace.model.Model;
import btrplace.model.SatConstraint;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.SolverException;

import java.util.Set;
import java.util.UUID;

/**
 * An interface to describe a constraint implementation for the solver.
 *
 * @author Fabien Hermenier
 */
public interface ChocoSatConstraint {

    /**
     * Inject the constraint into the problem.
     *
     * @param rp the problem
     * @throws SolverException if an error occurred while injecting.
     */
    void inject(ReconfigurationProblem rp) throws SolverException;

    /**
     * Get the constraint model associated to the implementation
     *
     * @return a {@link SatConstraint}
     */
    SatConstraint getAssociatedConstraint();

    public Set<UUID> getMisPlacedVMs(Model m);


    public boolean isSatisfied(ReconfigurationPlan plan);

}
